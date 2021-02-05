package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.common.model.DeletableEntity
import com.hookiesolutions.webhookie.common.model.UpdatableEntity
import com.hookiesolutions.webhookie.common.service.security.annotation.VerifyEntityCanBeDeleted
import com.hookiesolutions.webhookie.common.service.security.annotation.VerifyEntityCanBeUpdated
import com.hookiesolutions.webhookie.subscription.domain.Application.Queries.Companion.applicationConsumerGroupsIn
import com.hookiesolutions.webhookie.subscription.domain.Application.Queries.Companion.applicationConsumerGroupsIs
import com.hookiesolutions.webhookie.subscription.domain.Application.Queries.Companion.applicationsByEntity
import com.hookiesolutions.webhookie.subscription.domain.Application.Updates.Companion.pullConsumerGroup
import com.hookiesolutions.webhookie.subscription.domain.Application.Updates.Companion.setConsumerGroup
import com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationReadAccess
import com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationWriteAccess
import com.mongodb.client.result.UpdateResult
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
 * @since 27/1/21 18:48
 */
@Repository
class ApplicationRepository(
  private val mongoTemplate: ReactiveMongoTemplate,
) {
  fun save(application: Application): Mono<Application> {
    return mongoTemplate
      .save(application)
      .onErrorMap(DuplicateKeyException::class.java) {
        EntityExistsException(it.localizedMessage)
      }
  }

  fun userApplications(entity: String, userGroups: Collection<String>): Flux<Application> {
    val criteria = applicationsByEntity(entity)
      .andOperator(applicationConsumerGroupsIn(userGroups))
    return mongoTemplate
      .find(
        query(criteria),
        Application::class.java
      )
  }

  @VerifyApplicationReadAccess
  fun findByIdVerifyingReadAccess(id: String): Mono<Application> {
    return fetchById(id)
  }

  @VerifyApplicationWriteAccess
  fun findByIdVerifyingWriteAccess(id: String): Mono<Application> {
    return fetchById(id)
  }

  @VerifyEntityCanBeDeleted
  fun delete(deletableEntity: DeletableEntity<Application>): Mono<String> {
    return mongoTemplate.remove(deletableEntity.entity)
      .map { deletableEntity.entity.id!! }
  }

  @VerifyEntityCanBeUpdated
  fun update(updatableEntity: UpdatableEntity<Application>, id: String): Mono<Application> {
    return mongoTemplate
      .update(Application::class.java)
      .matching(query(byId(id)))
      .replaceWith(updatableEntity.entity)
      .withOptions(FindAndReplaceOptions.options().returnNew())
      .findAndReplace()
      .onErrorMap(DuplicateKeyException::class.java) {
        EntityExistsException(it.localizedMessage)
      }
  }

  private fun fetchById(id: String): Mono<Application> {
    return mongoTemplate.findById(id, Application::class.java)
      .switchIfEmpty(EntityNotFoundException("Application '$id' cannot be found").toMono())
  }

  fun removeConsumerGroup(value: String): Mono<UpdateResult> {
    return mongoTemplate
      .updateMulti(
        query(applicationConsumerGroupsIs(value)),
        pullConsumerGroup(value),
        Application::class.java
      )
  }

  fun updateConsumerGroup(oldValue: String, newValue: String): Mono<UpdateResult> {
    return mongoTemplate
      .updateMulti(
        query(applicationConsumerGroupsIs(oldValue)),
        setConsumerGroup(newValue),
        Application::class.java
      )
  }
}