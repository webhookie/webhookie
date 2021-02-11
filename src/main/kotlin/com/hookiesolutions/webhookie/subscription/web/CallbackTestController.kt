package com.hookiesolutions.webhookie.subscription.web

import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import com.hookiesolutions.webhookie.subscription.service.RequestValidator
import com.hookiesolutions.webhookie.subscription.service.model.CallbackValidationSampleRequest
import com.hookiesolutions.webhookie.subscription.web.SubscriptionAPIDocs.Companion.REQUEST_MAPPING_CALLBACKS
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import javax.validation.Valid

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 9/2/21 12:55
 */
@RestController
@SecurityRequirement(name = OAUTH2_SCHEME)
@RequestMapping(REQUEST_MAPPING_CALLBACKS)
class CallbackTestController(
  private val requestValidator: RequestValidator
) {
  @PostMapping(
    "test",
    consumes = [MediaType.ALL_VALUE],
    produces = [MediaType.ALL_VALUE]
  )
  fun callbackTester(
    @RequestBody @Valid requestBody: CallbackValidationSampleRequest
  ): Mono<ResponseEntity<ByteArray>> {
    return requestValidator.validateRequest(requestBody)
  }
}

