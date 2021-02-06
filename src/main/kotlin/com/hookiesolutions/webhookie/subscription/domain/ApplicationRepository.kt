package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.repository.GenericRepository
import com.hookiesolutions.webhookie.subscription.domain.Application.Queries.Companion.applicationConsumerGroupsIn
import com.hookiesolutions.webhookie.subscription.domain.Application.Queries.Companion.applicationConsumerGroupsIs
import com.hookiesolutions.webhookie.subscription.domain.Application.Queries.Companion.applicationsByEntity
import com.hookiesolutions.webhookie.subscription.domain.Application.Updates.Companion.pullConsumerGroup
import com.hookiesolutions.webhookie.subscription.domain.Application.Updates.Companion.setConsumerGroup
import com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationReadAccess
import com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationWriteAccess
import com.mongodb.client.result.UpdateResult
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 27/1/21 18:48
 */
@Repository
class ApplicationRepository(
  private val mongoTemplate: ReactiveMongoTemplate,
): GenericRepository<Application>(mongoTemplate, Application::class.java) {
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
    return findById(id)
  }

  @VerifyApplicationWriteAccess
  fun findByIdVerifyingWriteAccess(id: String): Mono<Application> {
    return findById(id)
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