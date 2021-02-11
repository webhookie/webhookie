package com.hookiesolutions.webhookie.subscription.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 11/2/21 15:27
 */
@RestControllerAdvice
class WebClientExceptionAdvice {
  @ExceptionHandler(WebClientRequestException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun handleWebClientRequestException(ex: WebClientRequestException): Mono<ResponseEntity<ByteArray>> {
    return ResponseEntity
      .badRequest()
      .body(ex.localizedMessage.encodeToByteArray())
      .toMono()
  }

  @ExceptionHandler(WebClientResponseException::class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  fun handleWebClientResponseException(ex: WebClientResponseException): Mono<ResponseEntity<ByteArray>> {
    return ResponseEntity
      .badRequest()
      .headers(ex.headers)
      .body(ex.responseBodyAsString.encodeToByteArray())
      .toMono()
  }
}