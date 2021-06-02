package com.hookiesolutions.webhookie.webhook.domain

import com.hookiesolutions.webhookie.common.repository.GenericRepository
import com.hookiesolutions.webhookie.common.repository.GenericRepository.Query.Companion.pageableWith
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_NUMBER_OF_WEBHOOKS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_WEBHOOKS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Queries.Companion.accessibleForGroups
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Queries.Companion.accessibleForProvider
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Queries.Companion.elemMatchByTopic
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Queries.Companion.webhookGroupTopicIs
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Updates.Companion.incNumberOfSubscriptions
import com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupReadAccess
import com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupWriteAccess
import com.mongodb.client.result.UpdateResult
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
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
    return mongoTemplate.find(
      query(accessibleForGroups(tokenGroups))
        .with(pageableWith(pageable, DEFAULT_SORT, DEFAULT_PAGE)),
      WebhookGroup::class.java
    )
  }

  @VerifyWebhookGroupReadAccess
  fun findByIdVerifyingReadAccess(id: String): Mono<WebhookGroup> {
    return findById(id)
  }

  @VerifyWebhookGroupReadAccess
  fun findByTopicVerifyingReadAccess(topic: String): Mono<WebhookGroup> {
    return mongoTemplate
      .findOne(
        query(webhookGroupTopicIs(topic)),
        WebhookGroup::class.java
      )
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

  fun myTopicsAsProvider(tokenGroups: List<String>): Flux<Topic> {
    val webhooksKey = "${'$'}$KEY_WEBHOOKS"
    val aggregation: Aggregation = Aggregation.newAggregation(
      Aggregation.match(accessibleForProvider(tokenGroups)),
      Aggregation.project(KEY_WEBHOOKS),
      Aggregation.unwind(webhooksKey),
      Aggregation.replaceRoot(webhooksKey),
    )
    return mongoTemplate
      .aggregate(
        aggregation,
        WebhookGroup::class.java,
        Webhook::class.java
      )
      .map { it.topic }
  }
                                
  fun incTopicSubscriptions(topic: String, number: Int): Mono<WebhookGroup> {
    return mongoTemplate.update(WebhookGroup::class.java)
      .matching(query(elemMatchByTopic(topic)))
      .apply(incNumberOfSubscriptions(number))
      .findAndModify()
  }

  companion object {
    private val DEFAULT_SORT = Sort.by(KEY_NUMBER_OF_WEBHOOKS).descending()
    private val DEFAULT_PAGE = PageRequest.of(0, 20)
  }
}
