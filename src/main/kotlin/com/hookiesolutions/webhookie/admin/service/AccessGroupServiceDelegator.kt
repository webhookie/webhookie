package com.hookiesolutions.webhookie.admin.service

import com.hookiesolutions.webhookie.admin.domain.AccessGroup
import com.hookiesolutions.webhookie.admin.service.model.SaveGroupRequest
import com.hookiesolutions.webhookie.common.message.entity.EntityDeletedMessage
import com.hookiesolutions.webhookie.common.message.entity.EntityUpdatedMessage
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class AccessGroupServiceDelegator<T: AccessGroup>(
  private val accessGroupService: AccessGroupService<T>,
  private val publisher: EntityEventPublisher
) {
  fun createAccessGroup(body: SaveGroupRequest): Mono<T> {
    return accessGroupService.createGroup(body)
  }

  fun allAccessGroups(): Flux<T> {
    return accessGroupService.allGroups()
  }

  fun getAccessGroup(id: String): Mono<T> {
    return accessGroupService.groupsById(id)
  }

  fun updateAccessGroup(id: String, bodyMono: SaveGroupRequest): Mono<T> {
    return accessGroupService.updateGroupsById(id, bodyMono)
      .doOnNext {
        val message = EntityUpdatedMessage(it.type, it.oldValue.iamGroupName, it.newValue.iamGroupName)
        publisher.publishUpdateEvent(message)
      }
      .map { it.newValue }
  }

  fun deleteAccessGroup(id: String): Mono<String> {
    return accessGroupService.deleteGroupsById(id)
      .map { EntityDeletedMessage(it.type, it.value.iamGroupName) }
      .doOnNext { publisher.publishDeleteEvent(it) }
      .map { "Deleted" }
  }
}