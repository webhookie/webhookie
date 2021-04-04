package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails
import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails.Keys.Companion.KEY_APPLICATION_ID
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionStatus
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import com.hookiesolutions.webhookie.subscription.domain.Application.Queries.Companion.applicationConsumerGroupsIn
import com.hookiesolutions.webhookie.subscription.domain.Application.Queries.Companion.applicationsByEntity
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage.Queries.Companion.bySubscriptionId
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_APPLICATION
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.SUBSCRIPTION_COLLECTION_NAME
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.applicationIdIs
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.callbackIdIs
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.isAuthorized
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.statusIsIn
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.topicIs
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.topicIsIn
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Updates.Companion.subscriptionStatusUpdate
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Updates.Companion.updateApplication
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Updates.Companion.updateCallback
import com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifySubscriptionProviderAccess
import com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifySubscriptionReadAccess
import com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifySubscriptionWriteAccess
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.slf4j.Logger
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.ConvertOperators
import org.springframework.data.mongodb.core.aggregation.Fields.UNDERSCORE_ID_REF
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 27/1/21 18:11
 */
@Repository
class SubscriptionRepository(
  private val log: Logger,
  private val mongoTemplate: ReactiveMongoTemplate,
) : GenericRepository<Subscription>(mongoTemplate, Subscription::class.java) {
  @VerifySubscriptionReadAccess
  fun findByIdVerifyingReadAccess(id: String): Mono<Subscription> {
    return findById(id)
  }

  @VerifySubscriptionWriteAccess
  fun findByIdVerifyingWriteAccess(id: String): Mono<Subscription> {
    return findById(id)
  }

  @VerifySubscriptionProviderAccess
  fun findByIdVerifyingProviderAccess(id: String): Mono<Subscription> {
    return findById(id)
  }

  fun findAllConsumerSubscriptions(
    entity: String,
    groups: List<String>,
    topic: String?,
    callbackId: String?
  ): Flux<Subscription> {
    val criteria = Criteria()
      .andOperator(
        applicationsByEntity(entity),
        applicationConsumerGroupsIn(groups)
      )
    val `as` = "subscriptions"
    val subscriptionsKey = "${'$'}subscriptions"
    val localField = "aId"
    val foreignField = "$KEY_APPLICATION.$KEY_APPLICATION_ID"

    val aggregation: Aggregation = Aggregation.newAggregation(
      Aggregation.match(criteria),
      Aggregation
        .addFields()
        .addFieldWithValue(localField, ConvertOperators.ToString.toString(UNDERSCORE_ID_REF))
        .build(),
      Aggregation.lookup(SUBSCRIPTION_COLLECTION_NAME, localField, foreignField, `as`),
      Aggregation.unwind(subscriptionsKey),
      Aggregation.replaceRoot(subscriptionsKey)
    )

    val subscriptionCriteriaList = mutableListOf<Criteria>()
    if(topic != null) {
      subscriptionCriteriaList.add(topicIs(topic))
    }
    if(callbackId != null) {
      subscriptionCriteriaList.add(callbackIdIs(callbackId))
    }
    if(subscriptionCriteriaList.isNotEmpty()) {
      aggregation.pipeline.add(Aggregation.match(Criteria().andOperator(*subscriptionCriteriaList.toTypedArray())))
    }

    if(log.isDebugEnabled) {
      log.debug("Webhook Traffic Aggregation query: '{}'", aggregation)
    }

    return mongoTemplate.aggregate(aggregation, Application::class.java, Subscription::class.java)
  }

  fun topicSubscriptions(topics: List<String>): Flux<Subscription> {
    return mongoTemplate
      .find(
        query(topicIsIn(topics)),
        Subscription::class.java
      )
  }

  fun findAuthorizedTopicSubscriptions(topic: String, authorizedSubscribers: Set<String>): Flux<Subscription> {
    val criteria = mutableListOf(
      topicIs(topic),
      statusIsIn(listOf(SubscriptionStatus.ACTIVATED, SubscriptionStatus.BLOCKED))
    )
    if (authorizedSubscribers.isNotEmpty()) {
      criteria.add(isAuthorized(authorizedSubscribers))
    }

    return mongoTemplate
      .find(query(Criteria().andOperator(*criteria.toTypedArray())), Subscription::class.java)
  }

  fun saveBlockedSubscriptionMessage(message: BlockedSubscriptionMessage): Mono<BlockedSubscriptionMessage> {
    return mongoTemplate.save(message)
  }

  fun findAllBlockedMessagesForSubscription(id: String): Flux<BlockedSubscriptionMessage> {
    return mongoTemplate.find(
      query(bySubscriptionId(id)),
      BlockedSubscriptionMessage::class.java
    )
  }

  fun deleteBlockedSubscriptionMessage(message: BlockedSubscriptionMessage): Mono<DeleteResult> {
    return mongoTemplate.remove(message)
  }

  fun updateCallbackSubscriptions(id: String, details: Any): Mono<UpdateResult> {
    return mongoTemplate.updateMulti(
      query(callbackIdIs(id)),
      updateCallback(details),
      Subscription::class.java
    )
  }

  fun updateApplicationSubscriptions(id: String, details: ApplicationDetails): Mono<UpdateResult> {
    return mongoTemplate.updateMulti(
      query(applicationIdIs(id)),
      updateApplication(details),
      Subscription::class.java
    )
  }

  fun statusUpdate(id: String, statusUpdate: StatusUpdate, validStatusList: List<SubscriptionStatus>): Mono<Subscription> {
    val criteria = byId(id)
      .andOperator(statusIsIn(validStatusList))
    return mongoTemplate
      .findAndModify(
        query(criteria),
        subscriptionStatusUpdate(statusUpdate),
        FindAndModifyOptions.options().returnNew(true),
        Subscription::class.java
      )
  }
}
