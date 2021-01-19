package com.hookiesolutions.webhookie.portal.service

import com.hookiesolutions.webhookie.portal.domain.AccessGroup
import com.hookiesolutions.webhookie.portal.service.model.SaveGroupRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class AccessGroupServiceDelegator<T: AccessGroup>(
  private val accessGroupService: AccessGroupService<T>
) {
  fun createAccessGroup(bodyMono: Mono<SaveGroupRequest>): Mono<T> {
    return accessGroupService.createGroup(bodyMono)
  }

  fun allAccessGroups(): Flux<T> {
    return accessGroupService.allGroups()
  }

  fun getAccessGroup(id: String): Mono<T> {
    return accessGroupService.groupsById(id)
  }

  fun updateAccessGroup(id: String, bodyMono: Mono<SaveGroupRequest>): Mono<T> {
    return accessGroupService.updateGroupsById(id, bodyMono)
  }

  fun deleteAccessGroup(id: String): Mono<String> {
    return accessGroupService.deleteGroupsById(id)
  }
}