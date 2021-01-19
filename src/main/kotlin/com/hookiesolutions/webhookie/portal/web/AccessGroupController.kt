package com.hookiesolutions.webhookie.portal.web

import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig
import com.hookiesolutions.webhookie.portal.domain.AccessGroup
import com.hookiesolutions.webhookie.portal.service.AccessGroupServiceDelegator
import com.hookiesolutions.webhookie.portal.service.model.SaveGroupRequest
import com.hookiesolutions.webhookie.portal.web.PortalAPIDocs.Companion.REQUEST_MAPPING_PORTAL_ADMIN
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.validation.Valid

@RequestMapping(REQUEST_MAPPING_PORTAL_ADMIN)
@SecurityRequirement(name = OpenAPIConfig.OAUTH2_SCHEME)
interface AccessGroupController<T: AccessGroup> {
  val serviceDelegator: AccessGroupServiceDelegator<T>

  @PostMapping(
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun createGroup(@RequestBody @Valid bodyMono: Mono<SaveGroupRequest>): Mono<T> {
    return serviceDelegator.createAccessGroup(bodyMono)
  }

  @GetMapping(
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun allGroups(): Flux<T> {
    return serviceDelegator.allAccessGroups()
  }

  @GetMapping(
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getGroup(id: String): Mono<T> {
    return serviceDelegator.getAccessGroup(id)
  }

  @PutMapping(
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun updateGroup(id: String, bodyMono: Mono<SaveGroupRequest>): Mono<T> {
    return serviceDelegator.updateAccessGroup(id, bodyMono)
  }

  @DeleteMapping(
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun deleteGroup(id: String): Mono<String> {
    return serviceDelegator.deleteAccessGroup(id)
  }
}

