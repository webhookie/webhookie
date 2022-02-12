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
import com.hookiesolutions.webhookie.common.model.RoleActor
import com.hookiesolutions.webhookie.common.model.RoleActor.Companion.PARAM_CONSUMER
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionApprovalDetails
import com.hookiesolutions.webhookie.subscription.service.SubscriptionService
import com.hookiesolutions.webhookie.subscription.service.model.subscription.ApproveSubscriptionRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.CreateSubscriptionRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.ReasonRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.RejectSubscriptionRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.UpdateSubscriptionRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.VerifySubscriptionRequest
import com.hookiesolutions.webhookie.subscription.web.SubscriptionAPIDocs.Companion.REQUEST_MAPPING_SUBSCRIPTIONS
import com.hookiesolutions.webhookie.subscription.service.model.subscription.SubscriptionApprovalRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.Logger
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.validation.Valid

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 18:20
 */
@RestController
@SecurityRequirement(name = OAUTH2_SCHEME)
@RequestMapping(REQUEST_MAPPING_SUBSCRIPTIONS)
class SubscriptionController(
  private val log: Logger,
  private val service: SubscriptionService,
) {
  @PostMapping(
    "",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  @ResponseStatus(HttpStatus.CREATED)
  fun createSubscription(@RequestBody @Valid request: CreateSubscriptionRequest): Mono<SubscriptionDTO> {
    return service.createSubscription(request)
      .map { it.dto() }
  }

  @GetMapping(
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun subscriptions(
    @RequestParam(required = true, defaultValue = PARAM_CONSUMER) role: RoleActor,
    @RequestParam(required = false) topic: String?,
    @RequestParam(required = false) callbackId: String?,
    pageable: Pageable
  ): Flux<SubscriptionDTO> {
    return service.subscriptions(role, pageable, topic, callbackId)
      .map { it.dto() }
  }

  @GetMapping(
    "/{id}",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getSubscription(@PathVariable id: String): Mono<SubscriptionDTO> {
    return service.subscriptionByIdVerifyingReadAccess(id)
      .map { it.dto()}
  }

  @DeleteMapping(
    "/{id}",
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun deleteSubscription(@PathVariable id: String): Mono<String> {
    return service.deleteSubscription(id)
  }

  @PutMapping(
    "/{id}",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun updateSubscription(
    @PathVariable id: String,
    @RequestBody @Valid request: UpdateSubscriptionRequest
  ): Mono<SubscriptionDTO> {
    return service.updateSubscription(id, request)
      .map { it.dto() }
  }

  @PostMapping(
    "/{id}/verify",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun verifySubscription(
    @PathVariable id: String,
    @RequestBody @Valid request: VerifySubscriptionRequest
  ): Mono<ResponseEntity<ByteArray>> {
    return service.verifySubscription(id, request)
  }

  @PostMapping(
    "/{id}/activate",
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun activateSubscription(
    @PathVariable id: String
  ): Mono<String> {
    return service.activateSubscription(id)
      .map { it.name }
  }

  @PostMapping(
    "/{id}/submit",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun submitSubscriptionForApproval(
    @PathVariable id: String,
    @RequestBody @Valid approvalRequest: SubscriptionApprovalRequest
  ): Mono<SubscriptionDTO> {
    return service.submitSubscriptionForApproval(id, approvalRequest)
      .map { it.dto() }
  }

  @PatchMapping(
    "/{id}/approve",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun approveSubmittedSubscription(
    @PathVariable id: String,
    @RequestBody @Valid request: ApproveSubscriptionRequest
  ): Mono<SubscriptionDTO> {
    return service.approveSubscription(id, request)
      .map { it.dto() }
  }

  @PatchMapping(
    "/{id}/reject",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun rejectSubmittedSubscription(
    @PathVariable id: String,
    @RequestBody @Valid request: RejectSubscriptionRequest
  ): Mono<SubscriptionDTO> {
    return service.rejectSubscription(id, request)
      .map { it.dto() }
  }

  @PostMapping(
    "/{id}/deactivate",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun deactivateSubscription(
    @PathVariable id: String,
    @RequestBody request: ReasonRequest
  ): Mono<String> {
    return service.deactivateSubscription(id, request)
      .map { it.name }
  }

  @PostMapping(
    "/{id}/suspend",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun suspendSubscription(
    @PathVariable id: String,
    @RequestBody request: ReasonRequest
  ): Mono<String> {
    return service.suspendSubscription(id, request)
      .map { it.name }
  }

  @PostMapping(
    "/{id}/unsuspend",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun unsuspendSubscription(
    @PathVariable id: String,
    @RequestBody request: ReasonRequest
  ): Mono<String> {
    return service.unsuspendSubscription(id, request)
      .map { it.name }
  }

  @PostMapping(
    "/{id}/unblock",
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun unblockSubscription(@PathVariable id: String): Mono<String> {
    log.info("Unblocking subscription: '{}'", id)
    return service.unblockSubscription(id)
      .map { it.name }
  }

  @PatchMapping(
    "/{id}/block",
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun blockSubscription(@PathVariable id: String): Mono<String> {
    log.info("Unblocking subscription: '{}'", id)
    val reason = "Blocked by user!"
    return service.blockSubscription(id, reason)
      .doOnNext {
        log.info("Subscription({}) was blocked because '{}'", id, reason)
      }
      .map { it.status.name }
  }

  @GetMapping(
    "/{id}/submitRequest",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun readSubmitRequest(@PathVariable id: String): Mono<SubscriptionApprovalDetails> {
    return service.readSubmitRequest(id)
  }
}
