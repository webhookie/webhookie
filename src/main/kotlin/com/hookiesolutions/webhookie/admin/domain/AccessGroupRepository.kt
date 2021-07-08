package com.hookiesolutions.webhookie.admin.domain

import com.hookiesolutions.webhookie.admin.domain.AccessGroup.Queries.Companion.iamGroupNameIs
import com.hookiesolutions.webhookie.common.annotation.Open
import com.hookiesolutions.webhookie.common.message.entity.EntityUpdatedMessage
import com.hookiesolutions.webhookie.common.model.DeletableEntity.Companion.deletable
import com.hookiesolutions.webhookie.common.model.UpdatableEntity.Companion.updatable
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
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
    return super.delete(deletable(group))
  }

  fun update(group: T, newGroup: T): Mono<EntityUpdatedMessage<T>> {
    return super.update(updatable(newGroup), group.id!!)
      .map { EntityUpdatedMessage(clazz.simpleName, group, it) }
  }

  fun findByIAMGroupName(iamGroupName: String): Mono<T> {
    return mongoTemplate.findOne(
      query(iamGroupNameIs(iamGroupName)),
      clazz
    )
  }
}
