package com.hookiesolutions.webhookie.admin.web

import com.hookiesolutions.webhookie.admin.domain.ProviderGroup
import com.hookiesolutions.webhookie.admin.service.ProviderGroupService
import com.hookiesolutions.webhookie.admin.service.model.SaveGroupRequest
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
  override val service: ProviderGroupService
): AccessGroupController<ProviderGroup> {

  @PostMapping(
    value = [REQUEST_MAPPING_PROVIDER_GROUPS]
  )
  override fun createGroup(@RequestBody @Valid body: SaveGroupRequest): Mono<ProviderGroup> {
    return super.createGroup(body)
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
  override fun updateGroup(@PathVariable id: String, @RequestBody @Valid bodyMono: SaveGroupRequest): Mono<ProviderGroup> {
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