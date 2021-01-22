package com.hookiesolutions.webhookie.admin.service

import com.hookiesolutions.webhookie.admin.domain.AccessGroup
import com.hookiesolutions.webhookie.admin.domain.AccessGroupRepository
import com.hookiesolutions.webhookie.admin.service.model.SaveGroupRequest
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_ADMIN
import com.hookiesolutions.webhookie.common.message.entity.EntityDeletedMessage
import com.hookiesolutions.webhookie.common.message.entity.EntityUpdatedMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 18:32
 */
class AccessGroupService<T : AccessGroup>(
  private val repository: AccessGroupRepository<T>,
  private val factory: AccessGroupFactory,
  private val clazz: Class<T>,
) {
  private val log: Logger = LoggerFactory.getLogger(AccessGroupService::class.java)

  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun createGroup(body: SaveGroupRequest): Mono<T> {
    return factory.createAccessGroup(body, clazz).toMono()
      .flatMap { repository.save(it) }
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
    return repository.findAll()
  }

  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun groupsById(id: String): Mono<T> {
    log.info("Fetching '{}' Access Group by id: '{}'", clazz.simpleName, id)
    return repository.findById(id)
  }

  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun deleteGroupsById(id: String): Mono<EntityDeletedMessage<T>> {
    log.info("Deleting '{}' Access Group by id: '{}'", clazz.simpleName, id)
    return repository.findById(id)
      .zipWhen { repository.delete(it) }
      .map { EntityDeletedMessage(clazz.simpleName, it.t1) }
      .doOnNext { log.info("{} with id '{}' was deleted successfully", clazz.simpleName, id) }
  }

  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun updateGroupsById(id: String, body: SaveGroupRequest): Mono<EntityUpdatedMessage<T>> {
    log.info("Updating '{}' Access Group by id: '{}'", clazz.simpleName, id)
    return repository.findById(id)
      .flatMap { repository.update(it, factory.createAccessGroup(body, clazz)) }
  }
}