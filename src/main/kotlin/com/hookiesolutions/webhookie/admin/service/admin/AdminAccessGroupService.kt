package com.hookiesolutions.webhookie.admin.service.admin

import com.hookiesolutions.webhookie.admin.domain.AccessGroup
import com.hookiesolutions.webhookie.admin.domain.AccessGroupRepository
import com.hookiesolutions.webhookie.admin.service.AccessGroupFactory
import com.hookiesolutions.webhookie.admin.service.AccessGroupService
import com.hookiesolutions.webhookie.admin.service.EntityEventPublisher
import com.hookiesolutions.webhookie.admin.service.model.SaveGroupRequest
import com.hookiesolutions.webhookie.common.Constants
import com.hookiesolutions.webhookie.common.annotation.Open
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import org.slf4j.Logger
import org.springframework.security.access.prepost.PreAuthorize
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/7/21 12:43
 */
@Open
class AdminAccessGroupService<T : AccessGroup>(
  override val repository: AccessGroupRepository<T>,
  override val factory: AccessGroupFactory,
  override val publisher: EntityEventPublisher,
  override val securityHandler: SecurityHandler,
  override val log: Logger,
  override val clazz: Class<T>,
): AccessGroupService<T>(repository, factory, publisher, securityHandler, log, clazz) {
  @PreAuthorize("hasAuthority('${Constants.Security.Roles.ROLE_ADMIN}')")
  override fun createGroup(body: SaveGroupRequest): Mono<T> {
    return super.createGroup(body)
  }

  @PreAuthorize("isAuthenticated()")
  override fun allGroups(): Flux<T> {
    return super.allGroups()
  }

  @PreAuthorize("hasAuthority('${Constants.Security.Roles.ROLE_ADMIN}')")
  override fun groupById(id: String): Mono<T> {
    return super.groupById(id)
  }

  @PreAuthorize("hasAuthority('${Constants.Security.Roles.ROLE_ADMIN}')")
  override fun groupByIAM(iamGroupName: String): Mono<T> {
    return super.groupByIAM(iamGroupName)
  }

  @PreAuthorize("hasAuthority('${Constants.Security.Roles.ROLE_ADMIN}')")
  override fun deleteGroupById(id: String): Mono<String> {
    return super.deleteGroupById(id)
  }

  @PreAuthorize("hasAuthority('${Constants.Security.Roles.ROLE_ADMIN}')")
  override fun updateGroupById(id: String, body: SaveGroupRequest): Mono<T> {
    return super.updateGroupById(id, body)
  }
}
