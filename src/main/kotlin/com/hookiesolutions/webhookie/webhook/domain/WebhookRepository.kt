package com.hookiesolutions.webhookie.webhook.domain

import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.common.model.DeletableEntity
import com.hookiesolutions.webhookie.common.model.UpdatableEntity
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Queries.Companion.accessibleForProviderWith
import com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupCanBeDeleted
import com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupCanBeUpdated
import com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupReadAccess
import com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupWriteAccess
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.FindAndReplaceOptions
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
      .onErrorMap(DuplicateKeyException::class.java) {
        val key = group.topics.toString()
        EntityExistsException(key, "Duplicate WebhookGroup topics provided: $key")
      }
  }

  fun findProviderWebhookGroups(myGroups: List<String>): Flux<WebhookGroup> {
    return mongoTemplate.find(
      query(accessibleForProviderWith(myGroups)),
      WebhookGroup::class.java
    )
  }

  @VerifyWebhookGroupReadAccess
  fun findByIdVerifyingReadAccess(id: String): Mono<WebhookGroup> {
    return fetchById(id)
  }

  @VerifyWebhookGroupWriteAccess
  fun findByIdVerifyingWriteAccess(id: String): Mono<WebhookGroup> {
    return fetchById(id)
  }

  @VerifyWebhookGroupCanBeDeleted
  fun delete(deletableWebhookGroup: DeletableEntity<WebhookGroup>): Mono<Void> {
    return repository.delete(deletableWebhookGroup.entity)
  }

  @VerifyWebhookGroupCanBeUpdated
  fun update(updatableEntity: UpdatableEntity<WebhookGroup>): Mono<WebhookGroup> {
    return mongoTemplate
      .update(WebhookGroup::class.java)
      .matching(query(byId(updatableEntity.entity.id)))
      .replaceWith(updatableEntity.entity)
      .withOptions(FindAndReplaceOptions.options().returnNew())
      .findAndReplace()
      .onErrorMap(DuplicateKeyException::class.java) {
        val key = updatableEntity.entity.topics.toString()
        EntityExistsException(key, "Duplicate WebhookGroup topics provided: $key")
      }
  }

  private fun fetchById(id: String): Mono<WebhookGroup> {
    return repository.findById(id)
      .switchIfEmpty { EntityNotFoundException("WebhookGroup with id: '{$id}' could not be found").toMono() }
  }
}