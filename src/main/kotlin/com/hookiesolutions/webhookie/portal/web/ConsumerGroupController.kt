package com.hookiesolutions.webhookie.portal.web

import com.hookiesolutions.webhookie.portal.domain.ConsumerGroup
import com.hookiesolutions.webhookie.portal.service.AccessGroupServiceDelegator
import com.hookiesolutions.webhookie.portal.service.model.SaveGroupRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
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
class ConsumerGroupController(
  override val serviceDelegator: AccessGroupServiceDelegator<ConsumerGroup>
): AccessGroupController<ConsumerGroup> {

  @PostMapping(
    value = [REQUEST_MAPPING_CONSUMER_GROUPS]
  )
  override fun createGroup(@RequestBody @Valid bodyMono: Mono<SaveGroupRequest>): Mono<ConsumerGroup> {
    return serviceDelegator.createAccessGroup(bodyMono)
  }

  @GetMapping(
    value = [REQUEST_MAPPING_CONSUMER_GROUPS]
  )
  override fun allGroups(): Flux<ConsumerGroup> {
    return serviceDelegator.allAccessGroups()
  }

  @GetMapping(
    value = ["$REQUEST_MAPPING_CONSUMER_GROUPS/{id}"]
  )
  override fun getGroup(@PathVariable id: String): Mono<ConsumerGroup> {
    return serviceDelegator.getAccessGroup(id)
  }

  @PutMapping(
    value = ["$REQUEST_MAPPING_CONSUMER_GROUPS/{id}"]
  )
  override fun updateGroup(@PathVariable id: String, @RequestBody @Valid bodyMono: Mono<SaveGroupRequest>): Mono<ConsumerGroup> {
    return serviceDelegator.updateAccessGroup(id, bodyMono)
  }

  @DeleteMapping(
    value = ["$REQUEST_MAPPING_CONSUMER_GROUPS/{id}"]
  )
  override fun deleteGroup(@PathVariable id: String): Mono<String> {
    return serviceDelegator.deleteAccessGroup(id)
  }

  companion object {
    const val REQUEST_MAPPING_CONSUMER_GROUPS = "/consumergroups"
  }
}