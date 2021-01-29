package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.subscription.domain.Application.Queries.Companion.applicationsByEntity
import org.springframework.dao.DuplicateKeyException
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
  fun findApplicationById(id: String): Mono<Application> {
    return mongoTemplate
      .findById(id, Application::class.java)
      .switchIfEmpty(EntityNotFoundException("Application not found by id: '$id'").toMono())
  }

  fun save(application: Application): Mono<Application> {
    return mongoTemplate
      .save(application)
      .onErrorMap(DuplicateKeyException::class.java) {
        EntityExistsException(application.name, "Duplicate Application: ${application.name}")
      }
  }

  fun userApplications(entity: String): Flux<Application> {
    return mongoTemplate
      .find(
        query(applicationsByEntity(entity)),
        Application::class.java
      )
  }

  fun findById(id: String): Mono<Application> {
    return mongoTemplate.findById(id, Application::class.java)
      .switchIfEmpty(EntityNotFoundException("Application '$id' cannot be found").toMono())
  }
}