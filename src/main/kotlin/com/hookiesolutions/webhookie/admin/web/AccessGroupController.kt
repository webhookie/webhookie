package com.hookiesolutions.webhookie.admin.web

import com.hookiesolutions.webhookie.admin.domain.AccessGroup
import com.hookiesolutions.webhookie.admin.service.AccessGroupService
import com.hookiesolutions.webhookie.admin.service.model.SaveGroupRequest
import com.hookiesolutions.webhookie.admin.web.AdminAPIDocs.Companion.REQUEST_MAPPING_ADMIN
import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.validation.Valid

@RequestMapping(REQUEST_MAPPING_ADMIN)
@SecurityRequirement(name = OAUTH2_SCHEME)
interface AccessGroupController<T: AccessGroup> {
  val service: AccessGroupService<T>

  @PostMapping(
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  @ResponseStatus(HttpStatus.CREATED)
  fun createGroup(@RequestBody @Valid body: SaveGroupRequest): Mono<T> {
    return service.createGroup(body)
  }

  @GetMapping(
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun allGroups(): Flux<T> {
    return service.allGroups()
  }

  @GetMapping(
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getGroup(id: String): Mono<T> {
    return service.groupById(id)
  }

  @PutMapping(
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun updateGroup(id: String, bodyMono: SaveGroupRequest): Mono<T> {
    return service.updateGroupById(id, bodyMono)
  }

  @DeleteMapping(
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun deleteGroup(id: String): Mono<String> {
    return service.deleteGroupById(id)
  }
}

