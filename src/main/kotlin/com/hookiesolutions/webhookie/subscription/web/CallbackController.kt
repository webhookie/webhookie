package com.hookiesolutions.webhookie.subscription.web

import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import com.hookiesolutions.webhookie.subscription.domain.Callback
import com.hookiesolutions.webhookie.subscription.service.CallbackService
import com.hookiesolutions.webhookie.subscription.service.model.CallbackRequest
import com.hookiesolutions.webhookie.subscription.web.SubscriptionAPIDocs.Companion.REQUEST_MAPPING_APPLICATIONS
import com.hookiesolutions.webhookie.subscription.web.SubscriptionAPIDocs.Companion.REQUEST_MAPPING_CALLBACKS
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
  ): Mono<Callback> {
    return service.createCallback(applicationId, body)
  }

  @GetMapping(
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getApplicationCallbacks(
    @PathVariable applicationId: String
  ): Flux<Callback> {
    return service.applicationCallbacks(applicationId)
  }

  @GetMapping(
    "/{id}",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getApplicationCallback(
    @PathVariable applicationId: String,
    @PathVariable id: String
  ): Mono<Callback> {
    return service.applicationCallbackById(applicationId, id)
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
  ): Mono<Callback> {
    return service.updateCallback(applicationId, id, body)
  }
}