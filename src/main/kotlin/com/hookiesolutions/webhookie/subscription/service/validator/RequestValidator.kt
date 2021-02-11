package com.hookiesolutions.webhookie.subscription.service.validator

import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.service.model.CallbackValidationSampleRequest
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 11/2/21 15:17
 */
@Service
class RequestValidator(
  private val log: Logger,
  private val timeMachine: TimeMachine
) {
  fun validateRequest(sampleRequest: CallbackValidationSampleRequest): Mono<ResponseEntity<ByteArray>> {
    log.info("Validating request to '{}'", sampleRequest.callback.requestTarget())
    return WebClient
      .create(sampleRequest.url)
      .method(sampleRequest.httpMethod)
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(sampleRequest.payload.encodeToByteArray()))
      .headers { sampleRequest.addMessageHeaders(it, timeMachine.now()) }
      .retrieve()
      .toEntity(ByteArray::class.java)
      .doOnNext { log.debug("Received '{}' response", it.statusCode) }
      .map {
        return@map if(it.hasBody()) {
          ResponseEntity
            .ok()
            .headers(it.headers)
            .body(it.body)
        } else {
          ResponseEntity
            .ok()
            .headers(it.headers)
            .build()
        }
      }
      .doOnError { log.warn("Validation Failed: '{}'", it.localizedMessage) }
  }
}