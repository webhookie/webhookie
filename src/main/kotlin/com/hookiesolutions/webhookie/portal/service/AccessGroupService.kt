package com.hookiesolutions.webhookie.portal.service

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_ADMIN
import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.portal.domain.ConsumerGroup
import com.hookiesolutions.webhookie.portal.service.model.CreateGroupRequest
import org.slf4j.Logger
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 18:32
 */
@Service
class AccessGroupService(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val log: Logger
) {
  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun createConsumerGroup(body: CreateGroupRequest): Mono<ConsumerGroup> {
    return mongoTemplate.insert(body.consumerGroup())
      .doOnSuccess {
        log.info("Consumer Group saved successfully with id: '{}'", it.id)
      }
      .onErrorMap(DuplicateKeyException::class.java) {
        EntityExistsException(body.iamGroupName, "Duplicate IAM Group mapping: ${body.iamGroupName}")
      }
      .doOnError {
        log.error(it.localizedMessage)
      }
  }

  fun allConsumerGroups(): Flux<ConsumerGroup> {
    log.info("Fetching all Consumer Groups...")
    return mongoTemplate.findAll(ConsumerGroup::class.java)
  }

  fun consumerGroupsById(id: String): Mono<ConsumerGroup> {
    log.info("Fetching Consumer Group by id: '{}'", id)
    return mongoTemplate.findById(id, ConsumerGroup::class.java)
      .switchIfEmpty(EntityNotFoundException("Consumer Group '$id' cannot be found").toMono())
  }
}