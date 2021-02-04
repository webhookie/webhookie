package com.hookiesolutions.webhookie.webhook.domain

import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.common.model.DeletableEntity
import com.hookiesolutions.webhookie.common.model.UpdatableEntity
import com.hookiesolutions.webhookie.common.service.security.annotation.VerifyEntityCanBeDeleted
import com.hookiesolutions.webhookie.common.service.security.annotation.VerifyEntityCanBeUpdated
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_NUMBER_OF_TOPICS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Queries.Companion.accessibleForGroups
import com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupReadAccess
import com.hookiesolutions.webhookie.webhook.service.security.annotation.VerifyWebhookGroupWriteAccess
import com.mongodb.client.result.UpdateResult
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
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
class WebhookGroupRepository(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val mongoRepository: WebhookGroupMongoRepository
) {
  fun save(group: WebhookGroup): Mono<WebhookGroup> {
    return mongoRepository.save(group)
      .onErrorMap(DuplicateKeyException::class.java) {
        EntityExistsException(it.localizedMessage)
      }
  }

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
    return fetchById(id)
  }

  @VerifyWebhookGroupWriteAccess
  fun findByIdVerifyingWriteAccess(id: String): Mono<WebhookGroup> {
    return fetchById(id)
  }

  @VerifyEntityCanBeDeleted
  fun delete(deletableWebhookGroup: DeletableEntity<WebhookGroup>): Mono<Void> {
    return mongoRepository.delete(deletableWebhookGroup.entity)
  }

  @VerifyEntityCanBeUpdated
  fun update(updatableEntity: UpdatableEntity<WebhookGroup>, id: String): Mono<WebhookGroup> {
    return mongoTemplate
      .update(WebhookGroup::class.java)
      .matching(query(byId(id)))
      .replaceWith(updatableEntity.entity)
      .withOptions(FindAndReplaceOptions.options().returnNew())
      .findAndReplace()
      .onErrorMap(DuplicateKeyException::class.java) {
        EntityExistsException(it.localizedMessage)
      }
  }

  private fun fetchById(id: String): Mono<WebhookGroup> {
    return mongoRepository.findById(id)
      .switchIfEmpty { EntityNotFoundException("WebhookGroup with id: '{$id}' could not be found").toMono() }
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

  companion object {
    private val DEFAULT_SORT = Sort.by(KEY_NUMBER_OF_TOPICS).descending()
  }
}