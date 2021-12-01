package com.hookiesolutions.webhookie.subscription.config.auth

import com.hookiesolutions.webhookie.subscription.service.model.CallbackValidationSampleRequest
import org.springframework.http.HttpHeaders
import reactor.core.publisher.Mono

abstract class AuthorizationHeaderProvider {
  abstract fun accepts(request: CallbackValidationSampleRequest): Boolean
  abstract fun customHeaders(request: CallbackValidationSampleRequest, headers: HttpHeaders): Mono<HttpHeaders>

  fun headers(request: CallbackValidationSampleRequest): Mono<HttpHeaders> {
    val h = HttpHeaders()
    request.headers.entries
      .forEach { entry ->
        val value = entry.value
        if(value is String) {
          h.add(entry.key, value)
        }
        @Suppress("UNCHECKED_CAST") val stringList = value as? List<String>
        if(stringList != null) {
          h.addAll(entry.key, stringList)
        }
      }

    return customHeaders(request, h)
  }
}
