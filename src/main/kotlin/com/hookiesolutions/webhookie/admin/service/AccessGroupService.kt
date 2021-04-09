package com.hookiesolutions.webhookie.admin.service

import com.hookiesolutions.webhookie.admin.domain.AccessGroup
import com.hookiesolutions.webhookie.admin.domain.AccessGroupRepository
import com.hookiesolutions.webhookie.admin.service.model.SaveGroupRequest
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_ADMIN
import com.hookiesolutions.webhookie.common.annotation.Open
import com.hookiesolutions.webhookie.common.message.entity.EntityDeletedMessage
import com.hookiesolutions.webhookie.common.message.entity.EntityUpdatedMessage
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import org.slf4j.Logger
import org.springframework.security.access.prepost.PreAuthorize
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuples

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 18:32
 */
@Open
abstract class AccessGroupService<T : AccessGroup>(
  val repository: AccessGroupRepository<T>,
  val factory: AccessGroupFactory,
  val publisher: EntityEventPublisher,
  val securityHandler: SecurityHandler,
  val log: Logger,
  val clazz: Class<T>,
) {
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

  @PreAuthorize("isAuthenticated()")
  fun allGroups(): Flux<T> {
    log.info("Fetching all '{}' Access Groups...", clazz.simpleName)
    return repository.findAll()
      .zipWith(securityHandler.groups())
      .filter { it.t2.contains(it.t1.iamGroupName) }
      .map { it.t1 }
  }

  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun groupById(id: String): Mono<T> {
    log.info("Fetching '{}' Access Group by id: '{}'", clazz.simpleName, id)
    return repository.findById(id)
  }

  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun deleteGroupById(id: String): Mono<String> {
    log.info("Deleting '{}' Access Group by id: '{}'", clazz.simpleName, id)
    return repository.findById(id)
      .zipWhen { repository.delete(it) }
      .doOnNext { log.info("{} with id '{}' was deleted successfully", clazz.simpleName, id) }
      .map { Tuples.of(EntityDeletedMessage(clazz.simpleName, it.t1.iamGroupName), it.t1) }
      .doOnNext { publisher.publishDeleteEvent(it.t1, it.t2) }
      .map { "Deleted" }
  }

  @PreAuthorize("hasAuthority('$ROLE_ADMIN')")
  fun updateGroupById(id: String, body: SaveGroupRequest): Mono<T> {
    log.info("Updating '{}' Access Group by id: '{}'", clazz.simpleName, id)
    return repository.findById(id)
      .flatMap { repository.update(it, factory.createAccessGroup(body, clazz)) }
      .doOnNext {
        val message = EntityUpdatedMessage(it.type, it.oldValue.iamGroupName, it.newValue.iamGroupName)
        publisher.publishUpdateEvent(message, it.newValue)
      }
      .map { it.newValue }
  }
}
