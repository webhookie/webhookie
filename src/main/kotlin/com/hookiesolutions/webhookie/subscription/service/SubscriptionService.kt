package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.common.service.IdGenerator
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.domain.ApplicationRepository
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionRepository
import com.hookiesolutions.webhookie.subscription.service.model.CreateSubscriptionRequest
import com.mongodb.client.result.DeleteResult
import org.slf4j.Logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


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
  private val manualPublisher: BlockedMessagePublisher,
  private val repository: SubscriptionRepository,
  private val applicationRepository: ApplicationRepository
) {
  fun findSubscriptionsFor(consumerMessage: ConsumerMessage): Flux<Subscription> {
    val topic = consumerMessage.headers.topic
    val authorizedSubscribers = consumerMessage.headers.authorizedSubscribers

    log.info("Reading '{}' subscribers for authorized subscribers: {}", topic, authorizedSubscribers)

    return repository
      .findAuthorizedTopicSubscriptions(topic, authorizedSubscribers)
  }

  fun saveBlockedSubscriptionMessage(message: BlockedSubscriptionMessage): Mono<BlockedSubscriptionMessage> {
    log.info("Saving BlockedSubscriptionMessage: '{}'", message.subscription.callback.url)
    return repository
      .saveBlockedSubscriptionMessage(message)
  }

  fun blockSubscriptionFor(message: PublisherErrorMessage): Mono<BlockedDetailsDTO> {
    val subscription = message.subscriptionMessage.subscription
    val time = timeMachine.now()
    val details = BlockedDetailsDTO(message.reason, time)

    log.info(
      "updating subscription: '{}' as and the reason is: '{}'",
      subscription.callback.url,
      details.reason
    )

    return repository
      .blockSubscriptionWithReason(subscription.id, details)
      .doOnNext {
        log.info("Subscription({}) was blocked because '{}'", subscription.id, details.reason)
      }
      .map { details }
  }

  fun findAllBlockedMessagesForSubscription(id: String): Flux<BlockedSubscriptionMessage> {
    log.info("Fetching all blocked messages for subscription: '{}'", id)
    return repository
      .findAllBlockedMessagesForSubscription(id)
  }

  fun unblockSubscriptionBy(id: String): Mono<Subscription> {
    return repository
      .unblockSubscription(id)
  }

  fun createSubscriptionFor(applicationId: String, body: CreateSubscriptionRequest): Mono<Subscription> {
    return applicationRepository
      .findApplicationById(applicationId)
      .map { factory.subscriptionFromCreateSubscriptionRequest(body, it) }
      .flatMap { repository.save(it) }
      .doOnNext { log.info("Subscription '{}' was created successfully", it.name) }
  }

  //TODO: refactor
  @Transactional
  fun resendAndRemoveSingleBlockedMessage(bsm: BlockedSubscriptionMessage): Mono<DeleteResult> {
    return repository
      .findSubscriptionById(bsm.subscription.id)
      .flatMap { manualPublisher.resendBlockedSubscriptionMessage(bsm, it) }
      .flatMap { repository.deleteBlockedSubscriptionMessage(bsm) }
  }

  fun signSubscriptionMessage(subscriptionMessage: SignableSubscriptionMessage): Mono<SignableSubscriptionMessage> {
    return repository
      .findSubscriptionById(subscriptionMessage.subscription.id)
      .flatMap { signor.sign(it, subscriptionMessage.originalMessage, idGenerator.generate()) }
      .map { factory.createSignedSubscriptionMessage(subscriptionMessage, it) }
  }
}