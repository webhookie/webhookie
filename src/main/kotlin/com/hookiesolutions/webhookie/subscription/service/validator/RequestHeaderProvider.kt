package com.hookiesolutions.webhookie.subscription.service.validator

import com.hookiesolutions.webhookie.subscription.config.auth.AuthorizationHeaderProvider
import com.hookiesolutions.webhookie.subscription.service.model.CallbackValidationSampleRequest
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RequestHeaderProvider(
  private val providers: List<AuthorizationHeaderProvider>
) {
  fun headers(sampleRequest: CallbackValidationSampleRequest): Mono<HttpHeaders> {
    return providers
      .first { it.accepts(sampleRequest) }
      .headers(sampleRequest)
  }
}
