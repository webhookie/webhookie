package com.hookiesolutions.webhookie.portal.service

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_ADMIN
import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.portal.domain.AccessGroup.Updates.Companion.updateGroupDetails
import com.hookiesolutions.webhookie.portal.domain.ConsumerGroup
import com.hookiesolutions.webhookie.portal.service.model.SaveGroupRequest
import org.slf4j.Logger
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
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
  fun createConsumerGroup(body: SaveGroupRequest): Mono<ConsumerGroup> {
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

  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun allConsumerGroups(): Flux<ConsumerGroup> {
    log.info("Fetching all Consumer Groups...")
    return mongoTemplate.findAll(ConsumerGroup::class.java)
  }

  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun consumerGroupsById(id: String): Mono<ConsumerGroup> {
    log.info("Fetching Consumer Group by id: '{}'", id)
    return mongoTemplate.findById(id, ConsumerGroup::class.java)
      .switchIfEmpty(EntityNotFoundException("Consumer Group '$id' cannot be found").toMono())
  }

  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun deleteConsumerGroupsById(id: String): Mono<Boolean> {
    log.info("Deleting Consumer Group by id: '{}'", id)
    return mongoTemplate.remove(query(byId(id)), ConsumerGroup::class.java)
      .map { it.deletedCount == 1L }
      .filter { it }
      .switchIfEmpty(EntityNotFoundException("Consumer Group '$id' cannot be found").toMono())
  }

  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun updateConsumerGroupsById(id: String, body: SaveGroupRequest): Mono<ConsumerGroup> {
    log.info("Updating Consumer Group by id: '{}'", id)
    return mongoTemplate
      .findAndModify(
        query(byId(id)),
        updateGroupDetails(body.consumerGroup()),
        FindAndModifyOptions.options().returnNew(true),
        ConsumerGroup::class.java
      )
      .onErrorMap(DuplicateKeyException::class.java) {
        EntityExistsException(body.iamGroupName, "Duplicate IAM Group mapping: ${body.iamGroupName}")
      }
      .switchIfEmpty(EntityNotFoundException("Consumer Group '$id' cannot be found").toMono())
  }
}