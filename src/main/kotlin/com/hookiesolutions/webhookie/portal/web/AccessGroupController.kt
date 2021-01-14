package com.hookiesolutions.webhookie.portal.web

import com.hookiesolutions.webhookie.portal.domain.ConsumerGroup
import com.hookiesolutions.webhookie.portal.service.AccessGroupService
import com.hookiesolutions.webhookie.portal.service.model.CreateGroupRequest
import com.hookiesolutions.webhookie.portal.web.AccessGroupController.Companion.REQUEST_MAPPING_PORTAL_ADMIN
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.validation.Valid

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 18:42
 */
@RestController
@RequestMapping(REQUEST_MAPPING_PORTAL_ADMIN)
class AccessGroupController(
  private val accessGroupService: AccessGroupService
) {
  @PostMapping(
    value = [REQUEST_MAPPING_CONSUMER_GROUPS],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun createConsumerGroup(@RequestBody @Valid bodyMono: Mono<CreateGroupRequest>): Mono<ConsumerGroup> {
    return bodyMono
      .flatMap { accessGroupService.createConsumerGroup(it) }
  }

  @GetMapping(
    value = [REQUEST_MAPPING_CONSUMER_GROUPS],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun allConsumerGroups(): Flux<ConsumerGroup> {
    return accessGroupService.allConsumerGroups()
  }

  @GetMapping(
    value = ["$REQUEST_MAPPING_CONSUMER_GROUPS/{id}"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getConsumerGroup(@PathVariable id: String): Mono<ConsumerGroup> {
    return accessGroupService.consumerGroupsById(id)
  }

  @PutMapping(
    value = ["$REQUEST_MAPPING_CONSUMER_GROUPS/{id}"],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun updateConsumerGroup(@PathVariable id: String, @RequestBody @Valid bodyMono: Mono<CreateGroupRequest>): Mono<ConsumerGroup> {
    return bodyMono
      .flatMap { accessGroupService.updateConsumerGroupsById(id, it) }
  }

  @DeleteMapping(
    value = ["$REQUEST_MAPPING_CONSUMER_GROUPS/{id}"],
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun deleteConsumerGroup(@PathVariable id: String): Mono<String> {
    return accessGroupService.deleteConsumerGroupsById(id)
      .map { it.toString() }
  }

  companion object {
    const val REQUEST_MAPPING_PORTAL_ADMIN = "/portal/admin"
    const val REQUEST_MAPPING_CONSUMER_GROUPS = "/consumergroups"
  }
}