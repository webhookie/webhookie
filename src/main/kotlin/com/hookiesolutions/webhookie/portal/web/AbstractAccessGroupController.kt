package com.hookiesolutions.webhookie.portal.web

import com.hookiesolutions.webhookie.portal.domain.AccessGroup
import com.hookiesolutions.webhookie.portal.domain.ConsumerGroup
import com.hookiesolutions.webhookie.portal.service.AccessGroupService
import com.hookiesolutions.webhookie.portal.service.model.SaveGroupRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

abstract class AbstractAccessGroupController<T: AccessGroup>(
  private val accessGroupService: AccessGroupService,
  private val clazz: Class<T>
) {
  fun createAccessGroup(bodyMono: Mono<SaveGroupRequest>): Mono<out AccessGroup> {
    return accessGroupService.createGroup(bodyMono, clazz)
  }

  fun allAccessGroups(): Flux<out AccessGroup> {
    return accessGroupService.allGroups(clazz)
  }

  fun getAccessGroup(id: String): Mono<out AccessGroup> {
    return accessGroupService.groupsById(id, clazz)
  }

  fun updateAccessGroup(id: String, bodyMono: Mono<SaveGroupRequest>): Mono<out AccessGroup> {
    return accessGroupService.updateGroupsById(id, bodyMono, clazz)
  }

  fun deleteAccessGroup(id: String): Mono<String> {
    return accessGroupService.deleteGroupsById(id, ConsumerGroup::class.java)
  }

  companion object {
    const val REQUEST_MAPPING_PORTAL_ADMIN = "/portal/admin"
    const val REQUEST_MAPPING_CONSUMER_GROUPS = "/consumergroups"
    const val REQUEST_MAPPING_PROVIDER_GROUPS = "/providergroups"
  }
}