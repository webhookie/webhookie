package com.hookiesolutions.webhookie.subscription.config.auth

import com.hookiesolutions.webhookie.subscription.service.model.CallbackValidationSampleRequest
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class NoneAuthorizationHeaderProvider(): AuthorizationHeaderProvider() {
  override fun accepts(request: CallbackValidationSampleRequest): Boolean {
    return request.securityScheme == null
  }

  override fun customHeaders(request: CallbackValidationSampleRequest, headers: HttpHeaders): Mono<HttpHeaders> {
    return headers.toMono()
  }

}

