package com.hookiesolutions.webhookie.portal.service

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_ADMIN
import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.portal.domain.AccessGroup
import com.hookiesolutions.webhookie.portal.domain.AccessGroup.Updates.Companion.updateGroupDetails
import com.hookiesolutions.webhookie.portal.service.model.SaveGroupRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.security.access.prepost.PreAuthorize
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 18:32
 */
class AccessGroupService<T: AccessGroup>(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val clazz: Class<T>
) {
  private val log: Logger = LoggerFactory.getLogger(AccessGroupService::class.java)
  
  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun createGroup(bodyMono: Mono<SaveGroupRequest>): Mono<T> {
    return bodyMono
      .flatMap { body ->
        mongoTemplate.insert(body.accessGroup(clazz))
          .onErrorMap(DuplicateKeyException::class.java) {
            EntityExistsException(body.iamGroupName, "Duplicate IAM Group mapping: ${body.iamGroupName}")
          }
      }
      .cast(clazz)
      .doOnSuccess {
        log.info("'{}' Access Group saved successfully with id: '{}'", clazz.simpleName, it.id)
      }
      .doOnError {
        log.error(it.localizedMessage)
      }
  }

  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun allGroups(): Flux<T> {
    log.info("Fetching all '{}' Access Groups...", clazz.simpleName)
    return mongoTemplate.findAll(clazz)
  }

  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun groupsById(id: String): Mono<T> {
    log.info("Fetching '{}' Access Group by id: '{}'", clazz.simpleName, id)
    return mongoTemplate.findById(id, clazz)
      .switchIfEmpty(EntityNotFoundException("Access Group '$id' cannot be found").toMono())
  }

  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun deleteGroupsById(id: String): Mono<String> {
    log.info("Deleting '{}' Access Group by id: '{}'", clazz.simpleName, id)
    return mongoTemplate.remove(query(byId(id)), clazz)
      .map { it.deletedCount == 1L }
      .filter { it }
      .switchIfEmpty(EntityNotFoundException("Access Group '$id' cannot be found").toMono())
      .map { it.toString() }
  }

  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun updateGroupsById(id: String, bodyMono: Mono<SaveGroupRequest>): Mono<T> {
    log.info("Updating '{}' Access Group by id: '{}'", clazz.simpleName, id)
    return bodyMono
      .flatMap { body ->
        mongoTemplate
          .findAndModify(
            query(byId(id)),
            updateGroupDetails(body.accessGroup(clazz)),
            FindAndModifyOptions.options().returnNew(true),
            clazz
          )
          .onErrorMap(DuplicateKeyException::class.java) {
            EntityExistsException(body.iamGroupName, "Duplicate IAM Group mapping: ${body.iamGroupName}")
          }
      }
      .switchIfEmpty(EntityNotFoundException("Access Group '$id' cannot be found").toMono())
  }
}