package com.hookiesolutions.webhookie.subscription.web

import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.common.model.RoleActor
import com.hookiesolutions.webhookie.common.model.RoleActor.Companion.PARAM_CONSUMER
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.domain.Subscription.Updates.Companion.blockSubscriptionUpdate
import com.hookiesolutions.webhookie.subscription.service.SubscriptionService
import com.hookiesolutions.webhookie.subscription.service.model.CreateSubscriptionRequest
import com.hookiesolutions.webhookie.subscription.service.model.UpdateSubscriptionRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.ReasonRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.ValidateSubscriptionRequest
import com.hookiesolutions.webhookie.subscription.web.SubscriptionAPIDocs.Companion.REQUEST_MAPPING_SUBSCRIPTIONS
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.Logger
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
  private val timeMachine: TimeMachine,
  private val mongoTemplate: ReactiveMongoTemplate,
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
    @RequestParam(required = true, defaultValue = PARAM_CONSUMER) role: RoleActor
  ): Flux<SubscriptionDTO> {
    return service.subscriptions(role)
      .map { it.dto() }
  }

  @GetMapping(
    "/{id}",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getSubscription(@PathVariable id: String): Mono<SubscriptionDTO> {
    return service.subscriptionById(id)
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
    TODO()
  }

  @PostMapping(
    "/{id}/validate",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun validateSubscription(
    @PathVariable id: String,
    @RequestBody @Valid request: ValidateSubscriptionRequest
  ): Mono<String> {
    return service.validateSubscription(id, request)
      .map { "Done" }
  }

  @PostMapping(
    "/{id}/activate",
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun activateSubscription(
    @PathVariable id: String
  ): Mono<String> {
    return service.activateSubscription(id)
      .map { "Done" }
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
      .map { "Done" }
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
      .map { "Done" }
  }

  @PatchMapping(
    "$REQUEST_MAPPING_SUBSCRIPTIONS/{id}/unblock",
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun unblockSubscription(@PathVariable id: String): Mono<String> {
    log.info("Unblocking subscription: '{}'", id)
    return service.unblockSubscriptionBy(id)
      .map { it.id!! }
  }

  @PatchMapping(
    "$REQUEST_MAPPING_SUBSCRIPTIONS/{id}/block",
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun blockSubscription(@PathVariable id: String): Mono<String> {
    log.info("Unblocking subscription: '{}'", id)
    val details = BlockedDetailsDTO("my Reason", timeMachine.now())
    return mongoTemplate
      .updateFirst(
        Query.query(byId(id)),
        blockSubscriptionUpdate(details),
        Subscription::class.java
      )
      .doOnNext {
        log.info("Subscription({}) was blocked because '{}'", id, details.reason)
      }
      .map { details.reason }
  }
}