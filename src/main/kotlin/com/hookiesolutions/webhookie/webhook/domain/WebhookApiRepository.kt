package com.hookiesolutions.webhookie.webhook.domain

import com.hookiesolutions.webhookie.common.repository.GenericRepository
import com.hookiesolutions.webhookie.common.repository.GenericRepository.Query.Companion.pageableWith
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Keys.Companion.KEY_NUMBER_OF_WEBHOOKS
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Keys.Companion.KEY_WEBHOOKS
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Queries.Companion.accessibleForGroups
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Queries.Companion.accessibleForProvider
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Queries.Companion.elemMatchByTopic
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Queries.Companion.webhookApiTopicIs
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Updates.Companion.incNumberOfSubscriptions
import com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookApiReadAccess
import com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookApiWriteAccess
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
class WebhookApiRepository(
  private val mongoTemplate: ReactiveMongoTemplate
): GenericRepository<WebhookApi>(mongoTemplate, WebhookApi::class.java) {
  fun findMyWebhookApis(tokenGroups: Collection<String>, pageable: Pageable): Flux<WebhookApi> {
    return mongoTemplate.find(
      query(accessibleForGroups(tokenGroups))
        .with(pageableWith(pageable, DEFAULT_SORT, DEFAULT_PAGE)),
      WebhookApi::class.java
    )
  }

  @VerifyWebhookApiReadAccess
  fun findByIdVerifyingReadAccess(id: String): Mono<WebhookApi> {
    return findById(id)
  }

  @VerifyWebhookApiReadAccess
  fun findByTopicVerifyingReadAccess(topic: String): Mono<WebhookApi> {
    return mongoTemplate
      .findOne(
        query(webhookApiTopicIs(topic)),
        WebhookApi::class.java
      )
  }

  @VerifyWebhookApiWriteAccess
  fun findByIdVerifyingWriteAccess(id: String): Mono<WebhookApi> {
    return findById(id)
  }

  fun removeAccessGroup(value: String, attr: String): Mono<UpdateResult> {
    return mongoTemplate
      .updateMulti(
        query(where(attr).`is`(value)),
        Update().pull(attr, value),
        WebhookApi::class.java
      )
  }

  fun updateAccessGroup(oldValue: String, newValue: String, attr: String): Mono<UpdateResult> {
    return mongoTemplate
      .updateMulti(
        query(where(attr).`is`(oldValue)),
        Update().set("$attr.$", newValue),
        WebhookApi::class.java
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
        WebhookApi::class.java,
        Webhook::class.java
      )
      .map { it.topic }
  }
                                
  fun incTopicSubscriptions(topic: String, number: Int): Mono<WebhookApi> {
    return mongoTemplate.update(WebhookApi::class.java)
      .matching(query(elemMatchByTopic(topic)))
      .apply(incNumberOfSubscriptions(number))
      .findAndModify()
  }

  companion object {
    private val DEFAULT_SORT = Sort.by(KEY_NUMBER_OF_WEBHOOKS).descending()
    private val DEFAULT_PAGE = PageRequest.of(0, 20)
  }
}
