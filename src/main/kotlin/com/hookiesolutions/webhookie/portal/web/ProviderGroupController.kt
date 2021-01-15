package com.hookiesolutions.webhookie.portal.web

import com.hookiesolutions.webhookie.portal.domain.ProviderGroup
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

@RestController
class ProviderGroupController(
  override val serviceDelegator: AccessGroupServiceDelegator<ProviderGroup>
): AccessGroupController<ProviderGroup> {

  @PostMapping(
    value = [REQUEST_MAPPING_PROVIDER_GROUPS]
  )
  override fun createGroup(@RequestBody @Valid bodyMono: Mono<SaveGroupRequest>): Mono<ProviderGroup> {
    return super.createGroup(bodyMono)
  }

  @GetMapping(
    value = [REQUEST_MAPPING_PROVIDER_GROUPS]
  )
  override fun allGroups(): Flux<ProviderGroup> {
    return super.allGroups()
  }

  @GetMapping(
    value = ["$REQUEST_MAPPING_PROVIDER_GROUPS/{id}"]
  )
  override fun getGroup(@PathVariable id: String): Mono<ProviderGroup> {
    return super.getGroup(id)
  }

  @PutMapping(
    value = ["$REQUEST_MAPPING_PROVIDER_GROUPS/{id}"]
  )
  override fun updateGroup(@PathVariable id: String, @RequestBody @Valid bodyMono: Mono<SaveGroupRequest>): Mono<ProviderGroup> {
    return super.updateGroup(id, bodyMono)
  }

  @DeleteMapping(
    value = ["$REQUEST_MAPPING_PROVIDER_GROUPS/{id}"]
  )
  override fun deleteGroup(@PathVariable id: String): Mono<String> {
    return super.deleteGroup(id)
  }

  companion object {
    const val REQUEST_MAPPING_PROVIDER_GROUPS = "/providergroups"
  }
}