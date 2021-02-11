package com.hookiesolutions.webhookie.subscription.web

import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionSignature
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.domain.CallbackDetails
import com.hookiesolutions.webhookie.subscription.utils.CryptoUtils
import com.hookiesolutions.webhookie.subscription.web.SubscriptionAPIDocs.Companion.REQUEST_MAPPING_CALLBACKS
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.bson.types.ObjectId
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.Instant
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
  private val timeMachine: TimeMachine
) {
  @PostMapping(
    "test",
    consumes = [MediaType.ALL_VALUE],
    produces = [MediaType.ALL_VALUE]
  )
  fun callbackTester(
    @RequestBody @Valid requestBody: SampleRequest
  ): Mono<ResponseEntity<ByteArray>> {
    return WebClient
      .create(requestBody.url)
      .method(requestBody.httpMethod)
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody.payload.encodeToByteArray()))
      .headers { requestBody.addMessageHeaders(it, timeMachine.now()) }
      .retrieve()
      .toEntity(ByteArray::class.java)
      .map {
        return@map if(it.hasBody()) {
          ResponseEntity
            .status(it.statusCode)
            .headers(it.headers)
            .body(it.body)
        } else {
          ResponseEntity
            .status(it.statusCode)
            .headers(it.headers)
            .build()
        }
      }
      .onErrorResume(WebClientRequestException::class.java) {
        ResponseEntity
          .status(HttpStatus.BAD_GATEWAY)
          .headers(it.headers)
          .body(it.localizedMessage.encodeToByteArray())
          .toMono()
      }
      .onErrorResume(WebClientResponseException::class.java) {
        ResponseEntity
          .status(it.statusCode)
          .headers(it.headers)
          .body(it.responseBodyAsString.encodeToByteArray())
          .toMono()
      }
      .onErrorResume {
        ResponseEntity
          .status(HttpStatus.BAD_GATEWAY)
          .body(it.localizedMessage.encodeToByteArray())
          .toMono()
      }
  }
}

data class SampleRequest(
  val httpMethod: HttpMethod,
  val url: String,
  val payload: String,
  val headers: HttpHeaders,
  val secret: String? = null,
  val traceId: String? = null,
  val spanId: String? = null
) {

  val callback: CallbackDetails
    get() = CallbackDetails("id", "TEMP", httpMethod, url)

  fun addMessageHeaders(
    httpHeaders: HttpHeaders,
    time: Instant
  ) {
    val traceId = this.traceId?: ObjectId.get().toHexString()
    val spanId = this.spanId?: ObjectId.get().toHexString()
    Mono
      .create<String> { it.success(secret) }
      .flatMap { CryptoUtils.hmac(it, callback, time.toString(), traceId, spanId) }
      .map {
        SubscriptionSignature.Builder()
          .keyId("keyId")
          .algorithm(CryptoUtils.ALG)
          .traceId(traceId)
          .spanId(spanId)
          .date(time)
          .signature(it)
          .build()
      }
      .subscribe { signature ->
        signature.headers
          .forEach {
            httpHeaders.add(it.key, it.value)
          }
      }

    httpHeaders.addAll(headers)
  }
}
