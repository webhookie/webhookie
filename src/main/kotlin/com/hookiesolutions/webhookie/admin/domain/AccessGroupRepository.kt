package com.hookiesolutions.webhookie.admin.domain

import com.hookiesolutions.webhookie.common.annotation.Open
import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.common.message.entity.EntityUpdatedMessage
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.mongodb.client.result.DeleteResult
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 22/1/21 15:17
 */
@Open
abstract class AccessGroupRepository<T: AccessGroup>(
  val mongoTemplate: ReactiveMongoTemplate,
  val clazz: Class<T>
) {
  fun save(group: T): Mono<T> {
    return mongoTemplate.save(group)
      .onErrorMap(DuplicateKeyException::class.java) {
        EntityExistsException(it.localizedMessage)
      }
  }

  fun findAll(): Flux<T> {
    return mongoTemplate.findAll(clazz)
  }

  fun findById(id: String): Mono<T> {
    return mongoTemplate.findById(id, clazz)
      .switchIfEmpty(EntityNotFoundException("${clazz.simpleName} '$id' cannot be found").toMono())
  }

  fun delete(group: T): Mono<DeleteResult> {
    return mongoTemplate.remove(group)
  }

  fun update(group: T, newGroup: T): Mono<EntityUpdatedMessage<T>> {
    return mongoTemplate
      .update(clazz)
      .matching(query(byId(group.id)))
      .replaceWith(newGroup)
      .withOptions(FindAndReplaceOptions.options().returnNew())
      .findAndReplace()
      .onErrorMap(DuplicateKeyException::class.java) {
        EntityExistsException(it.localizedMessage)
      }
      .map { EntityUpdatedMessage(clazz.simpleName, group, it) }
  }
}