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

package com.hookiesolutions.webhookie.subscription.web

import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import com.hookiesolutions.webhookie.common.model.dto.CallbackDTO
import com.hookiesolutions.webhookie.subscription.service.CallbackService
import com.hookiesolutions.webhookie.subscription.service.model.CallbackRequest
import com.hookiesolutions.webhookie.subscription.web.SubscriptionAPIDocs.Companion.REQUEST_MAPPING_APPLICATIONS
import com.hookiesolutions.webhookie.subscription.web.SubscriptionAPIDocs.Companion.REQUEST_MAPPING_CALLBACKS
import com.hookiesolutions.webhookie.subscription.web.model.response.NoOfActiveCallbackSubscriptionsResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.validation.Valid

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/2/21 01:37
 */

@RestController
@SecurityRequirement(name = OAUTH2_SCHEME)
@RequestMapping("$REQUEST_MAPPING_APPLICATIONS/{applicationId}$REQUEST_MAPPING_CALLBACKS")
class CallbackController(
  private val service: CallbackService
) {
  @PostMapping(
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  @ResponseStatus(HttpStatus.CREATED)
  fun createCallback(
    @Valid @RequestBody body: CallbackRequest,
    @PathVariable applicationId: String
  ): Mono<CallbackDTO> {
    return service.createCallback(applicationId, body)
      .map { it.dto() }
  }

  @GetMapping(
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getApplicationCallbacks(
    @PathVariable applicationId: String
  ): Flux<CallbackDTO> {
    return service.applicationCallbacks(applicationId)
      .map { it.dto() }
  }

  @GetMapping(
    "/{id}",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getApplicationCallback(
    @PathVariable applicationId: String,
    @PathVariable id: String
  ): Mono<CallbackDTO> {
    return service.applicationCallbackById(applicationId, id)
      .map { it.dto() }
  }

  @DeleteMapping(
    "/{id}",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun deleteApplicationCallback(
    @PathVariable applicationId: String,
    @PathVariable id: String
  ): Mono<String> {
    return service.deleteApplicationCallbackById(applicationId, id)
  }

  @PutMapping(
    "/{id}",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun updateApplicationCallback(
    @Valid @RequestBody body: CallbackRequest,
    @PathVariable applicationId: String,
    @PathVariable id: String
  ): Mono<CallbackDTO> {
    return service.updateCallback(applicationId, id, body)
      .map { it.dto() }
  }

  @GetMapping(
    "/{callbackId}/noOfSubscriptions",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun noOfCallbackSubscriptions(
    @PathVariable callbackId: String,
    @PathVariable applicationId: String
  ): Mono<NoOfActiveCallbackSubscriptionsResponse> {
    return service.countActiveSubscriptions(applicationId, callbackId)
      .map { NoOfActiveCallbackSubscriptionsResponse(it) }
  }
}
