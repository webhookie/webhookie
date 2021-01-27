package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 27/1/21 18:48
 */
@Repository
class ApplicationRepository(
  private val mongoTemplate: ReactiveMongoTemplate
) {
  fun findApplicationById(id: String): Mono<Application> {
    return mongoTemplate.findById(id, Application::class.java)
      .switchIfEmpty(EntityNotFoundException("Application not found by id: '$id'").toMono())
  }
}