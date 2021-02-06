package com.hookiesolutions.webhookie.admin.domain

import com.hookiesolutions.webhookie.common.annotation.Open
import com.hookiesolutions.webhookie.common.message.entity.EntityUpdatedMessage
import com.hookiesolutions.webhookie.common.model.DeletableEntity
import com.hookiesolutions.webhookie.common.model.UpdatableEntity
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 22/1/21 15:17
 */
@Open
abstract class AccessGroupRepository<T: AccessGroup>(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val clazz: Class<T>
): GenericRepository<T>(mongoTemplate, clazz) {
  fun findAll(): Flux<T> {
    return mongoTemplate.findAll(clazz)
  }

  fun delete(group: T): Mono<String> {
    return super.delete(DeletableEntity.deletable(group))
  }

  fun update(group: T, newGroup: T): Mono<EntityUpdatedMessage<T>> {
    return super.update(UpdatableEntity.updatable(newGroup), group.id!!)
      .map { EntityUpdatedMessage(clazz.simpleName, group, it) }
  }
}