package com.hookiesolutions.webhookie.admin.service.admin

import com.hookiesolutions.webhookie.admin.domain.AccessGroup
import com.hookiesolutions.webhookie.admin.service.AccessGroupService
import com.hookiesolutions.webhookie.admin.service.model.SaveGroupRequest
import com.hookiesolutions.webhookie.common.Constants
import com.hookiesolutions.webhookie.common.annotation.Open
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
  val service: AccessGroupService<T>,
) {
  @PreAuthorize("hasAuthority('${Constants.Security.Roles.ROLE_ADMIN}')")
  fun createGroup(body: SaveGroupRequest): Mono<T> {
    return service.createGroup(body)
  }

  @PreAuthorize("isAuthenticated()")
  fun allGroups(): Flux<T> {
    return service.allGroups()
  }

  @PreAuthorize("hasAuthority('${Constants.Security.Roles.ROLE_ADMIN}')")
  fun groupById(id: String): Mono<T> {
    return service.groupById(id)
  }

  @PreAuthorize("hasAuthority('${Constants.Security.Roles.ROLE_ADMIN}')")
  fun groupByIAM(iamGroupName: String): Mono<T> {
    return service.groupByIAM(iamGroupName)
  }

  @PreAuthorize("hasAuthority('${Constants.Security.Roles.ROLE_ADMIN}')")
  fun deleteGroupById(id: String): Mono<String> {
    return service.deleteGroupById(id)
  }

  @PreAuthorize("hasAuthority('${Constants.Security.Roles.ROLE_ADMIN}')")
  fun updateGroupById(id: String, body: SaveGroupRequest): Mono<T> {
    return service.updateGroupById(id, body)
  }
}
