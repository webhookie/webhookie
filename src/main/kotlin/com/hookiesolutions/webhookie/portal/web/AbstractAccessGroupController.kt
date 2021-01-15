package com.hookiesolutions.webhookie.portal.web

import com.hookiesolutions.webhookie.portal.domain.AccessGroup
import com.hookiesolutions.webhookie.portal.service.AccessGroupService
import com.hookiesolutions.webhookie.portal.service.model.SaveGroupRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

abstract class AbstractAccessGroupController<T: AccessGroup>(
  private val accessGroupService: AccessGroupService,
  private val clazz: Class<T>
) {
  fun createAccessGroup(bodyMono: Mono<SaveGroupRequest>): Mono<T> {
    return accessGroupService.createGroup(bodyMono, clazz)
      .cast(clazz)
  }

  fun allAccessGroups(): Flux<T> {
    return accessGroupService.allGroups(clazz)
      .cast(clazz)
  }

  fun getAccessGroup(id: String): Mono<T> {
    return accessGroupService.groupsById(id, clazz)
      .cast(clazz)
  }

  fun updateAccessGroup(id: String, bodyMono: Mono<SaveGroupRequest>): Mono<T> {
    return accessGroupService.updateGroupsById(id, bodyMono, clazz)
      .cast(clazz)
  }

  fun deleteAccessGroup(id: String): Mono<String> {
    return accessGroupService.deleteGroupsById(id, clazz)
  }
}