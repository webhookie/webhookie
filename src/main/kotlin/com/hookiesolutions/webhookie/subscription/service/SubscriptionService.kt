package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.idIsIn
import com.hookiesolutions.webhookie.subscription.domain.Company.Keys.Companion.KEY_SUBSCRIPTIONS
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.topicIs
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import org.bson.Document
import org.slf4j.Logger
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.Aggregation.replaceRoot
import org.springframework.data.mongodb.core.aggregation.Aggregation.unwind
import org.springframework.data.mongodb.core.aggregation.Fields.UNDERSCORE_ID
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux


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
    val match = match(
      idIsIn(consumerMessage.authorizedSubscribers)
        .and(KEY_SUBSCRIPTIONS)
        .elemMatch(topicIs(consumerMessage.topic))
    )

    val project = project()
      .andExclude(UNDERSCORE_ID)
      .and {
        val filterExpression: DBObject = BasicDBObject()
        filterExpression.put("input", "${'$'}$KEY_SUBSCRIPTIONS")
        filterExpression.put("as", "subs")
        filterExpression.put(
          "cond",
          BasicDBObject("${'$'}eq", listOf("${'$'}${'$'}subs.topic", consumerMessage.topic)
        ))
        Document("${'$'}filter", filterExpression)
      }
      .`as`("subs")

    val unwind = unwind("${'$'}subs")
    val replaceRoot = replaceRoot("${'$'}subs")
    val aggregation = Aggregation.newAggregation(
      match,
      project,
      unwind,
      replaceRoot
    )

    return mongoTemplate
      .aggregate(aggregation, "company", Subscription::class.java)
  }
}