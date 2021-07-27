/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.admin.web.admin

import com.hookiesolutions.webhookie.admin.domain.AccessGroup
import com.hookiesolutions.webhookie.admin.service.admin.AdminAccessGroupService
import com.hookiesolutions.webhookie.admin.service.model.SaveGroupRequest
import com.hookiesolutions.webhookie.admin.web.admin.AdminAPIDocs.Companion.REQUEST_MAPPING_ADMIN
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
interface AdminAccessGroupController<T: AccessGroup> {
  val service: AdminAccessGroupService<T>

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

