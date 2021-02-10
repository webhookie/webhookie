package com.hookiesolutions.webhookie.webhook.domain

import com.hookiesolutions.webhookie.common.repository.GenericRepository
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.KEY_TOPIC
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Keys.Companion.SUBSCRIPTION_COLLECTION_NAME
import com.hookiesolutions.webhookie.webhook.domain.Topic.Keys.Companion.KEY_TOPIC_NAME
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_NUMBER_OF_TOPICS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_TOPICS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Queries.Companion.accessibleForAllProviders
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Queries.Companion.accessibleForGroups
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Queries.Companion.providerGroupsIn
import com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupReadAccess
import com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupWriteAccess
import com.mongodb.client.result.UpdateResult
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 16:42
 */
@Repository
class WebhookGroupRepository(
  private val mongoTemplate: ReactiveMongoTemplate
): GenericRepository<WebhookGroup>(mongoTemplate, WebhookGroup::class.java) {
  fun findMyWebhookGroups(tokenGroups: Collection<String>, pageable: Pageable): Flux<WebhookGroup> {
    val query = query(accessibleForGroups(tokenGroups))
    if(pageable.isUnpaged) {
      query.with(DEFAULT_SORT)
    } else {
      query.with(pageable)
    }
    return mongoTemplate.find(
      query,
      WebhookGroup::class.java
    )
  }

  @VerifyWebhookGroupReadAccess
  fun findByIdVerifyingReadAccess(id: String): Mono<WebhookGroup> {
    return findById(id)
  }

  @VerifyWebhookGroupWriteAccess
  fun findByIdVerifyingWriteAccess(id: String): Mono<WebhookGroup> {
    return findById(id)
  }

  fun removeAccessGroup(value: String, attr: String): Mono<UpdateResult> {
    return mongoTemplate
      .updateMulti(
        query(where(attr).`is`(value)),
        Update().pull(attr, value),
        WebhookGroup::class.java
      )
  }

  fun updateAccessGroup(oldValue: String, newValue: String, attr: String): Mono<UpdateResult> {
    return mongoTemplate
      .updateMulti(
        query(where(attr).`is`(oldValue)),
        Update().set("$attr.$", newValue),
        WebhookGroup::class.java
      )
  }

  fun mySubscriptionsAsProvider(tokenGroups: List<String>): Flux<Subscription> {
    val criteria = Criteria()
      .orOperator(
        accessibleForAllProviders(),
        providerGroupsIn(tokenGroups)
      )
    val asField = "subscriptions"
    val subscriptionsKey = "${'$'}$asField"
    val topicsKey = "${'$'}$KEY_TOPICS"
    val agg: Aggregation = Aggregation.newAggregation(
      Aggregation.match(criteria),
      Aggregation.project(KEY_TOPICS),
      Aggregation.unwind(topicsKey),
      Aggregation.replaceRoot(topicsKey),
      Aggregation.lookup(SUBSCRIPTION_COLLECTION_NAME, KEY_TOPIC_NAME, KEY_TOPIC, asField),
      Aggregation.unwind(subscriptionsKey),
      Aggregation.replaceRoot(subscriptionsKey)
    )
    return mongoTemplate.aggregate(
      agg,
      WebhookGroup::class.java,
      Subscription::class.java
    )
  }

  companion object {
    private val DEFAULT_SORT = Sort.by(KEY_NUMBER_OF_TOPICS).descending()
  }
}