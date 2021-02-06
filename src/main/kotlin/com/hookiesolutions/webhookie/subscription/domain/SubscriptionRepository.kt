package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage.Queries.Companion.bySubscriptionId
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.isAuthorized
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.topicIs
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Updates.Companion.blockSubscriptionUpdate
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Updates.Companion.unblockSubscriptionUpdate
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 27/1/21 18:11
 */
@Repository
class SubscriptionRepository(
  private val mongoTemplate: ReactiveMongoTemplate
): GenericRepository<Subscription>(mongoTemplate, Subscription::class.java) {
  fun findAuthorizedTopicSubscriptions(topic: String, authorizedSubscribers: Set<String>): Flux<Subscription> {
    var criteria = topicIs(topic)
    if(authorizedSubscribers.isNotEmpty()) {
      criteria = criteria.andOperator(isAuthorized(authorizedSubscribers))
    }

    return mongoTemplate
      .find(query(criteria), Subscription::class.java)
  }

  fun saveBlockedSubscriptionMessage(message: BlockedSubscriptionMessage): Mono<BlockedSubscriptionMessage> {
    return mongoTemplate.save(message)
  }

  fun blockSubscriptionWithReason(id: String, details: BlockedDetailsDTO): Mono<UpdateResult> {
    return mongoTemplate
      .updateFirst(
        query(byId(id)),
        blockSubscriptionUpdate(details),
        Subscription::class.java
      )
  }

  fun findAllBlockedMessagesForSubscription(id: String): Flux<BlockedSubscriptionMessage> {
    return mongoTemplate.find(
      query(bySubscriptionId(id)),
      BlockedSubscriptionMessage::class.java
    )
  }

  fun unblockSubscription(id: String): Mono<Subscription> {
    return mongoTemplate
      .findAndModify(
        query(byId(id)),
        unblockSubscriptionUpdate(),
        FindAndModifyOptions.options().returnNew(true),
        Subscription::class.java
      )
  }

  fun findSubscriptionById(id: String): Mono<Subscription> {
    return mongoTemplate.findById(id, Subscription::class.java)
      .switchIfEmpty { EntityNotFoundException("Subscription could not be found: $id").toMono() }
  }

  fun deleteBlockedSubscriptionMessage(message: BlockedSubscriptionMessage): Mono<DeleteResult> {
    return mongoTemplate.remove(message)
  }
}