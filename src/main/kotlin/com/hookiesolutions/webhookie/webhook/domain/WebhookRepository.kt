package com.hookiesolutions.webhookie.webhook.domain

import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Queries.Companion.accessibleForProviderWith
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 16:42
 */
@Repository
class WebhookRepository(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val repository: WebhookGroupRepository
) {
  fun save(group: WebhookGroup): Mono<WebhookGroup> {
    return repository.save(group)
  }

  fun findProviderWebhookGroups(myGroups: List<String>): Flux<WebhookGroup> {
    return mongoTemplate.find(
      query(accessibleForProviderWith(myGroups)),
      WebhookGroup::class.java
    )
  }
}