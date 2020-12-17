package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.subscription.UnsuccessfulSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.topicIs
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Updates.Companion.blockSubscription
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Updates.Companion.unblockSubscription
import org.slf4j.Logger
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Service
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
  private val mongoTemplate: ReactiveMongoTemplate
) {
  fun findSubscriptionsFor(consumerMessage: ConsumerMessage): Flux<Subscription> {
    log.info("Reading '{}' subscribers", consumerMessage.headers.topic)

    return mongoTemplate.find(
      query(topicIs(consumerMessage.headers.topic)),
      Subscription::class.java
    )
  }

  fun saveBlockedSubscription(message: BlockedSubscriptionMessage): Mono<BlockedSubscriptionMessage> {
    log.info("Saving BlockedSubscriptionMessage: '{}'", message.subscription.callbackUrl)
    return mongoTemplate.save(message)
  }

  fun blockSubscriptionFor(message: UnsuccessfulSubscriptionMessage): Mono<BlockedSubscriptionMessage> {
    val subscription = message.subscriptionMessage.subscription
    val details = BlockedDetailsDTO(message.reason, message.time)

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
      .map { BlockedSubscriptionMessage.from(message) }
  }

  fun findAllAndRemoveBlockedMessagesForSubscription(id: String): Flux<BlockedSubscriptionMessage> {
    log.info("Fetching all blocked messages for subscription: '{}'", id)
    val query = query(BlockedSubscriptionMessage.Queries.bySubscriptionId(id))
    return mongoTemplate.findAllAndRemove(query, BlockedSubscriptionMessage::class.java)
  }

  fun unblockSubscriptionBy(id: String): Mono<Subscription> {
    return mongoTemplate.findAndModify(
      query(byId(id)),
      unblockSubscription(),
      FindAndModifyOptions.options().returnNew(true),
      Subscription::class.java
    )
  }
}