package com.hookiesolutions.webhookie.portal.web

import com.hookiesolutions.webhookie.portal.domain.ConsumerGroup
import com.hookiesolutions.webhookie.portal.service.AccessGroupService
import com.hookiesolutions.webhookie.portal.service.model.CreateGroupRequest
import com.hookiesolutions.webhookie.portal.web.AccessGroupController.Companion.REQUEST_MAPPING_PORTAL_ADMIN
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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

  companion object {
    const val REQUEST_MAPPING_PORTAL_ADMIN = "/portal/admin"
    const val REQUEST_MAPPING_CONSUMER_GROUPS = "/consumergroups"
  }
}