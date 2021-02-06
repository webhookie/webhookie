package com.hookiesolutions.webhookie.common.repository

import com.hookiesolutions.webhookie.common.annotation.Open
import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.common.model.DeletableEntity
import com.hookiesolutions.webhookie.common.model.UpdatableEntity
import com.hookiesolutions.webhookie.common.service.security.annotation.VerifyEntityCanBeDeleted
import com.hookiesolutions.webhookie.common.service.security.annotation.VerifyEntityCanBeUpdated
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 6/2/21 19:24
 */
@Open
abstract class GenericRepository<E: AbstractEntity>(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val clazz: Class<E>
) {
  fun save(e: E): Mono<E> {
    return mongoTemplate
      .save(e)
      .onErrorMap(DuplicateKeyException::class.java) {
        EntityExistsException(it.localizedMessage)
      }
  }

  fun findById(id: String): Mono<E> {
    return mongoTemplate
      .findById(id, clazz)
      .switchIfEmpty(EntityNotFoundException("${clazz.simpleName} not found by id: '$id'").toMono())
  }

  @VerifyEntityCanBeDeleted
  fun delete(deletableEntity: DeletableEntity<E>): Mono<String> {
    return mongoTemplate.remove(deletableEntity.entity)
      .map { deletableEntity.entity.id!! }
  }

  @VerifyEntityCanBeUpdated
  fun update(updatableEntity: UpdatableEntity<E>, id: String): Mono<E> {
    updatableEntity.entity.version = updatableEntity.entity.version?.plus(1)
    return mongoTemplate
      .update(clazz)
      .matching(query(byId(id)))
      .replaceWith(updatableEntity.entity)
      .withOptions(FindAndReplaceOptions.options().returnNew())
      .findAndReplace()
      .onErrorMap(DuplicateKeyException::class.java) {
        EntityExistsException(it.localizedMessage)
      }
  }
}