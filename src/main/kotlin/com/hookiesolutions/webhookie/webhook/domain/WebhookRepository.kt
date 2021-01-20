package com.hookiesolutions.webhookie.webhook.domain

import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Queries.Companion.accessibleForProviderWith
import com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupConsumeAccess
import com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupWriteAccess
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

  @VerifyWebhookGroupConsumeAccess
  fun findByIdVerifyingReadAccess(id: String): Mono<WebhookGroup> {
    return fetchById(id)
  }

  @VerifyWebhookGroupWriteAccess
  fun findByIdVerifyingWriteAccess(id: String): Mono<WebhookGroup> {
    return fetchById(id)
  }

  fun delete(entity: WebhookGroup): Mono<Void> {
    return repository.delete(entity)
  }

  private fun fetchById(id: String): Mono<WebhookGroup> {
    return repository.findById(id)
      .switchIfEmpty { EntityNotFoundException("WebhookGroup with id: '{$id}' could not be found").toMono() }
  }
}