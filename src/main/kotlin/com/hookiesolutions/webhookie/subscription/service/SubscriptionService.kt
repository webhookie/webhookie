package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.common.service.IdGenerator
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage.Queries.Companion.bySubscriptionId
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_COMPANY_ID
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.topicIs
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Updates.Companion.blockSubscription
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Updates.Companion.unblockSubscription
import com.hookiesolutions.webhookie.subscription.service.model.CreateSubscriptionRequest
import org.slf4j.Logger
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/12/20 15:09
 */
@Service
class SubscriptionService(
  private val log: Logger,
  private val timeMachine: TimeMachine,
  private val signor: SubscriptionSignor,
  private val idGenerator: IdGenerator,
  private val factory: ConversionsFactory,
  private val signSubscriptionMessageChannel: MessageChannel,
  private val mongoTemplate: ReactiveMongoTemplate
) {
  fun findSubscriptionsFor(consumerMessage: ConsumerMessage): Flux<Subscription> {
    log.info("Reading '{}' subscribers", consumerMessage.headers.topic)

    var criteria = topicIs(consumerMessage.headers.topic)
    if(consumerMessage.headers.authorizedSubscribers.isNotEmpty()) {
      criteria = criteria
        .and(KEY_COMPANY_ID).
        `in`(consumerMessage.headers.authorizedSubscribers)
    }

    return mongoTemplate.find(
      query(criteria),
      Subscription::class.java
    )
  }

  fun saveBlockedSubscriptionMessage(message: BlockedSubscriptionMessage): Mono<BlockedSubscriptionMessage> {
    log.info("Saving BlockedSubscriptionMessage: '{}'", message.subscription.callbackUrl)
    return mongoTemplate.save(message)
  }

  fun blockSubscriptionFor(message: PublisherErrorMessage): Mono<BlockedDetailsDTO> {
    val subscription = message.subscriptionMessage.subscription
    val time = timeMachine.now()
    val details = BlockedDetailsDTO(message.reason, time)

    log.info(
      "updating subscription: '{}' as and the reason is: '{}'",
      subscription.callbackUrl,
      details.reason
    )

    return mongoTemplate
      .updateFirst(
        query(byId(subscription.id)),
        blockSubscription(details),
        Subscription::class.java
      )
      .doOnNext {
        log.info("Subscription({}) was blocked because '{}'", subscription.id, details.reason)
      }
      .map { details }
  }

  fun findAllBlockedMessagesForSubscription(id: String): Flux<BlockedSubscriptionMessage> {
    log.info("Fetching all blocked messages for subscription: '{}'", id)
    val query = query(bySubscriptionId(id))
    return mongoTemplate.find(query, BlockedSubscriptionMessage::class.java)
  }

  fun unblockSubscriptionBy(id: String): Mono<Subscription> {
    return mongoTemplate.findAndModify(
      query(byId(id)),
      unblockSubscription(),
      FindAndModifyOptions.options().returnNew(true),
      Subscription::class.java
    )
  }

  fun createSubscriptionFor(applicationId: String, body: CreateSubscriptionRequest): Mono<Subscription> {
    return mongoTemplate.findById(applicationId, Application::class.java)
      .switchIfEmpty(EntityNotFoundException("Application not found by id: '$applicationId'").toMono())
      .map { factory.createSubscriptionRequestToSubscription(body, it) }
      .flatMap { mongoTemplate.save(it) }
      .doOnNext { log.info("Subscription '{}' was created successfully", it.name) }
  }

  //TODO: refactor
  @Transactional
  fun resendAndRemoveSingleBlockedMessage(bsm: BlockedSubscriptionMessage) {
    val subscriptionMessage = factory.blockedSubscriptionMessageToSubscriptionMessage(bsm)
    val message = MessageBuilder
      .withPayload(subscriptionMessage)
      .copyHeadersIfAbsent(bsm.messageHeaders)
      .build()
    signSubscriptionMessageChannel.send(message)
      .toMono()
      .flatMap { mongoTemplate.remove(bsm) }
      .subscribe {
        if (it.deletedCount > 0) {
          log.info("Blocked Message: '{}' was removed successfully!", bsm.id)
        } else {
          log.warn("Was unable to remove Blocked Message: '{}'!", bsm.id)
        }
      }
  }

  fun signSubscriptionMessage(subscriptionMessage: SubscriptionMessage): Mono<SubscriptionMessage> {
    return mongoTemplate.findById(subscriptionMessage.subscription.id, Subscription::class.java)
      .map {
        val spanId = idGenerator.generate()
        val signature = signor.sign(it, subscriptionMessage.originalMessage, spanId)

        subscriptionMessage.copy(
          spanId = spanId,
          signature = signature
        )
      }
  }
}