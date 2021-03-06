/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails
import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails.Keys.Companion.KEY_APPLICATION_ID
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionStatus
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import com.hookiesolutions.webhookie.subscription.domain.Application.Queries.Companion.applicationManagersIncludes
import com.hookiesolutions.webhookie.subscription.domain.Application.Queries.Companion.applicationsByEntity
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage.Queries.Companion.bySubscriptionId
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_APPLICATION
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.SUBSCRIPTION_COLLECTION_NAME
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.applicationIdIs
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.callbackIdIs
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.isAuthorized
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.statusIsIn
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.subscriptionIsActive
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Queries.Companion.subscriptionIsDraft
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
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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
import reactor.util.function.Tuple2
import reactor.util.function.Tuples


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
    userId: String,
    requestedPageable: Pageable,
    topic: String?,
    callbackId: String?
  ): Flux<Subscription> {
    val criteria = Criteria()
      .andOperator(
        applicationsByEntity(entity),
        applicationManagersIncludes(userId)
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

    sort(aggregation, requestedPageable, SUBSCRIPTION_DEFAULT_SORT, SUBSCRIPTION_DEFAULT_PAGE)

    if(log.isDebugEnabled) {
      log.debug("Webhook Traffic Aggregation query: '{}'", aggregation)
    }

    return mongoTemplate.aggregate(aggregation, Application::class.java, Subscription::class.java)
  }

  fun topicSubscriptions(
    topic: String?,
    topics: List<String>,
    ignoreTopicsFilter: Boolean,
    requestedPageable: Pageable
  ): Flux<Subscription> {
    val pageable = Query.pageableWith(requestedPageable, SUBSCRIPTION_DEFAULT_SORT, SUBSCRIPTION_DEFAULT_PAGE)
    var criteria = if(ignoreTopicsFilter) {
      Criteria()
    } else {
      topicIsIn(topics)
    }
    if(topic != null) {
      criteria = criteria.andOperator(topicIs(topic))
    }
    return mongoTemplate
      .find(
        query(criteria).with(pageable),
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
    return mongoTemplate.remove(
      query(byId(message.id)),
      BlockedSubscriptionMessage::class.java
    )
  }

  fun updateCallbackSubscriptions(id: String, details: Any?): Mono<UpdateResult> {
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

  fun statusUpdate(
    id: String,
    statusUpdate: StatusUpdate,
    validStatusList: List<SubscriptionStatus>,
    vararg updates: Pair<String, Any>
  ): Mono<Subscription> {
    val criteria = byId(id).andOperator(statusIsIn(validStatusList))
    val update = subscriptionStatusUpdate(statusUpdate)
    updates.forEach {
      update.set(it.first, it.second)
    }
    return mongoTemplate
      .findAndModify(
        query(criteria),
        update,
        FindAndModifyOptions.options().returnNew(true),
        Subscription::class.java
      )
  }

  fun countActiveSubscriptionsByCallbackId(callbackId: String): Mono<Long> {
    return mongoTemplate.count(
      query(callbackIdIs(callbackId).andOperator(subscriptionIsActive())),
      Subscription::class.java
    )
  }

  fun suspendAllFor(topics: List<String>, statusUpdate: StatusUpdate): Mono<Tuple2<Long, Long>> {
    return mongoTemplate
      .updateMulti(
        query(topicIsIn(topics)),
        subscriptionStatusUpdate(statusUpdate),
        Subscription::class.java
      )
      .map {
        Tuples.of(it.matchedCount, it.modifiedCount)
      }
  }

  fun findDraftSubscription(topic: String, callbackId: String): Mono<Subscription> {
    return mongoTemplate.findOne(
      query(
        Criteria().andOperator(
          topicIs(topic),
          callbackIdIs(callbackId),
          subscriptionIsDraft()
        )
      ),
      Subscription::class.java
    )
  }

  companion object {
    val SUBSCRIPTION_DEFAULT_SORT = Sort.by("${Subscription.Keys.KEY_STATUS_UPDATE}.${StatusUpdate.Keys.KEY_TIME}").descending()
    val SUBSCRIPTION_DEFAULT_PAGE = PageRequest.of(0, 50)
  }
}
