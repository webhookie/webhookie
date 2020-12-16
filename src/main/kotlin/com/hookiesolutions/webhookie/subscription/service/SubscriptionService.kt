package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.subscription.UnsuccessfulSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.elemMatch
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscription
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscription.Keys.Companion.KEY_BLOCKED_MESSAGE_COLLECTION_NAME
import com.hookiesolutions.webhookie.subscription.domain.Company
import com.hookiesolutions.webhookie.subscription.domain.Company.Keys.Companion.KEY_COMPANY_COLLECTION_NAME
import com.hookiesolutions.webhookie.subscription.domain.Company.Keys.Companion.KEY_SUBSCRIPTIONS
import com.hookiesolutions.webhookie.subscription.domain.Company.Queries.Companion.bySubscriptionId
import com.hookiesolutions.webhookie.subscription.domain.Company.Updates.Companion.blockSubscription
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_TOPIC
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.topicIs
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import org.bson.Document
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.Aggregation.replaceRoot
import org.springframework.data.mongodb.core.aggregation.Aggregation.unwind
import org.springframework.data.mongodb.core.aggregation.Fields.UNDERSCORE_ID
import org.springframework.data.mongodb.core.query.Criteria
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
    log.info("Reading '{}' subscribers", consumerMessage.topic)

    val match = match(matchCriteria(consumerMessage))
    val project = project()
      .andExclude(UNDERSCORE_ID)
      .and { subsFilter(consumerMessage) }
      .`as`(KEY_SUBSCRIPTIONS)
    val unwind = unwind("\$$KEY_SUBSCRIPTIONS")
    val replaceRoot = replaceRoot("\$$KEY_SUBSCRIPTIONS")

    val aggregation = Aggregation.newAggregation(
      match,
      project,
      unwind,
      replaceRoot
    )

    return mongoTemplate
      .aggregate(aggregation, KEY_COMPANY_COLLECTION_NAME, Subscription::class.java)
  }

  fun saveBlockedSubscription(message: BlockedSubscription): Mono<BlockedSubscription> {
    log.info("Saving BlockedSubscriptionMessage: '{}'", message.subscription.callbackUrl)
    return mongoTemplate.save(message, KEY_BLOCKED_MESSAGE_COLLECTION_NAME)
  }

  fun blockSubscriptionFor(message: UnsuccessfulSubscriptionMessage): Mono<BlockedSubscription> {
    val subscription = message.subscriptionMessage.subscription
    val details = BlockedDetailsDTO(message.reason, message.time)

    log.info(
      "updating subscription: '{}' as and the reason is: '{}'",
      subscription.callbackUrl,
      details.reason
    )

    return mongoTemplate
      .updateFirst(
        query(bySubscriptionId(subscription.id)),
        blockSubscription(details),
        Company::class.java
      )
      .doOnNext {
        log.info("Subscription({}) was blocked because '{}'", subscription.id, details.reason)
      }
      .map { BlockedSubscription.from(message) }
  }

  fun findAllAndRemoveBlockedMessagesForSubscription(id: String): Flux<BlockedSubscription> {
    log.info("Fetching all blocked messages for subscription: '{}'", id)
    val query = query(BlockedSubscription.Queries.bySubscriptionId(id))
    return mongoTemplate.findAllAndRemove(query, BlockedSubscription::class.java, KEY_BLOCKED_MESSAGE_COLLECTION_NAME)
  }

  private fun matchCriteria(consumerMessage: ConsumerMessage): Criteria {
    var criteria = elemMatch(KEY_SUBSCRIPTIONS, topicIs(consumerMessage.topic))
    if (consumerMessage.authorizedSubscribers.isNotEmpty()) {
      val objectIds = consumerMessage.authorizedSubscribers
        .filter { ObjectId.isValid(it) }
        .map { ObjectId(it) }
      criteria = criteria.and(UNDERSCORE_ID).`in`(objectIds)
    }

    return criteria
  }

  private fun subsFilter(consumerMessage: ConsumerMessage): Document {
    val filterExpression: DBObject = BasicDBObject()
    filterExpression.put("input", "\$$KEY_SUBSCRIPTIONS")
    filterExpression.put("as", KEY_SUBSCRIPTIONS)
    filterExpression.put(
      "cond",
      BasicDBObject(
        "\$eq", listOf("\$\$$KEY_SUBSCRIPTIONS.$KEY_TOPIC", consumerMessage.topic)
      )
    )
    return Document("\$filter", filterExpression)
  }
}