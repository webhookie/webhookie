package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.common.model.DeletableEntity
import com.hookiesolutions.webhookie.common.model.UpdatableEntity
import com.hookiesolutions.webhookie.common.service.security.annotation.VerifyEntityCanBeDeleted
import com.hookiesolutions.webhookie.common.service.security.annotation.VerifyEntityCanBeUpdated
import com.hookiesolutions.webhookie.subscription.domain.Callback.Queries.Companion.applicationIdIs
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/2/21 01:33
 */
@Repository
class CallbackRepository(
  private val mongoTemplate: ReactiveMongoTemplate
) {
  fun findById(id: String): Mono<Callback> {
    return mongoTemplate
      .findById(id, Callback::class.java)
      .switchIfEmpty(EntityNotFoundException("Callback not found by id: '$id'").toMono())
  }

  fun save(callback: Callback): Mono<Callback> {
    return mongoTemplate
      .save(callback)
      .onErrorMap(DuplicateKeyException::class.java) {
        EntityExistsException(it.localizedMessage)
      }
  }

  fun findApplicationCallbacks(applicationId: String): Flux<Callback> {
    return mongoTemplate
      .find(
        query(applicationIdIs(applicationId)),
        Callback::class.java
      )
  }

  @VerifyEntityCanBeDeleted
  fun delete(deletableEntity: DeletableEntity<Callback>): Mono<String> {
    return mongoTemplate.remove(deletableEntity.entity)
      .map { deletableEntity.entity.id!! }
  }

  @VerifyEntityCanBeUpdated
  fun update(updatableEntity: UpdatableEntity<Callback>, id: String): Mono<Callback> {
    updatableEntity.entity.version = updatableEntity.entity.version?.plus(1)
    return mongoTemplate
      .update(Callback::class.java)
      .matching(query(byId(id)))
      .replaceWith(updatableEntity.entity)
      .withOptions(FindAndReplaceOptions.options().returnNew())
      .findAndReplace()
      .onErrorMap(DuplicateKeyException::class.java) {
        EntityExistsException(it.localizedMessage)
      }
  }
}