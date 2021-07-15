package com.hookiesolutions.webhookie.audit.web

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.domain.SpanStatus
import com.hookiesolutions.webhookie.audit.domain.TraceStatus
import com.hookiesolutions.webhookie.audit.service.SpanService
import com.hookiesolutions.webhookie.audit.service.TraceService
import com.hookiesolutions.webhookie.audit.web.TrafficAPIDocs.Companion.REQUEST_MAPPING_TRAFFIC
import com.hookiesolutions.webhookie.audit.web.model.request.SpanRequest
import com.hookiesolutions.webhookie.audit.web.model.request.TraceRequest
import com.hookiesolutions.webhookie.audit.web.model.response.SpanResponse
import com.hookiesolutions.webhookie.audit.web.model.response.SpanResponseBody
import com.hookiesolutions.webhookie.audit.web.model.response.TraceRequestBody
import com.hookiesolutions.webhookie.audit.web.model.response.TraceResponse
import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import com.hookiesolutions.webhookie.common.service.TimeMachine
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/3/21 19:54
 */

@RestController
@SecurityRequirement(name = OAUTH2_SCHEME)
@RequestMapping(REQUEST_MAPPING_TRAFFIC)
class TrafficController(
  private val spanService: SpanService,
  private val traceService: TraceService,
  private val timeMachine: TimeMachine
) {
  @GetMapping(
    REQUEST_MAPPING_TRAFFIC_SPAN,
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun userSpans(
    @RequestParam(required = false) traceId: String?,
    @RequestParam(required = false) spanId: String?,
    @RequestParam(required = false) application: String?,
    @RequestParam(required = false) entity: String?,
    @RequestParam(required = false) callback: String?,
    @RequestParam(required = false) topic: String?,
    @RequestParam(required = false, defaultValue = "") status: List<SpanStatus>,
    @RequestParam(required = false) from: Instant?,
    @RequestParam(required = false) to: Instant?,
    pageable: Pageable
  ): Flux<SpanResponse> {
    val request = SpanRequest.Builder()
      .traceId(traceId)
      .spanId(spanId)
      .application(application)
      .entity(entity)
      .callback(callback)
      .topic(topic)
      .status(status)
      .from(from)
      .to(to)
      .build()
    return spanService.userSpans(pageable, request)
      .map { SpanResponse.from(it)}
  }

  @GetMapping(
    REQUEST_MAPPING_TRAFFIC_TRACE,
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun userTraces(
    @RequestParam(required = false) traceId: String?,
    @RequestParam(required = false) applicationId: String?,
    @RequestParam(required = false) entity: String?,
    @RequestParam(required = false) callbackId: String?,
    @RequestParam(required = false) topic: String?,
    @RequestParam(required = false, defaultValue = "") status: List<TraceStatus>,
    @RequestParam(required = false) from: Instant?,
    @RequestParam(required = false) to: Instant?,
    pageable: Pageable
  ): Flux<TraceResponse> {
    val request = TraceRequest.Builder()
      .traceId(traceId)
      .applicationId(applicationId)
      .entity(entity)
      .callbackId(callbackId)
      .topic(topic)
      .status(status)
      .from(from)
      .to(to)
      .build()
    return traceService.userTraces(pageable, request)
      .map { TraceResponse.from(it)}
  }

  @GetMapping(
    "$REQUEST_MAPPING_TRAFFIC_TRACE/{traceId}/spans",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun traceSpans(
    @PathVariable traceId: String,
    @RequestParam(required = false) applicationId: String?,
    @RequestParam(required = false) entity: String?,
    @RequestParam(required = false) callbackId: String?,
    pageable: Pageable
  ): Flux<SpanResponse> {
    val request = TraceRequest.Builder()
      .applicationId(applicationId)
      .entity(entity)
      .callbackId(callbackId)
      .build()
    return spanService.traceSpans(pageable, traceId, request)
      .map { SpanResponse.from(it)}
  }

  @GetMapping(
    "$REQUEST_MAPPING_TRAFFIC_SPAN/{spanId}/response",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun spanResponse(@PathVariable spanId: String): Mono<SpanResponseBody> {
    return spanService.fetchSpanVerifyingReadAccess(spanId)
      .flatMap { SpanResponseBody.from(it) }
      .switchIfEmpty { SpanResponseBody.notReady(spanId, timeMachine.now()) }
  }

  @GetMapping(
    "$REQUEST_MAPPING_TRAFFIC_TRACE/{traceId}/request",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun traceRequest(@PathVariable traceId: String): Mono<TraceRequestBody> {
    return traceService.fetchTraceVerifyingReadAccess(traceId )
      .map { TraceRequestBody.from(it) }
  }

  @GetMapping(
    "$REQUEST_MAPPING_TRAFFIC_SPAN/{spanId}",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getSpan(@PathVariable spanId: String): Mono<Span> {
    return spanService.fetchSpanVerifyingReadAccess(spanId)
  }

  @PostMapping(
    "$REQUEST_MAPPING_TRAFFIC_SPAN/resend",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun resendSpan(@RequestBody spanIds: List<String>): Mono<String> {
    return spanService.resend(spanIds)
      .map { it.size.toString() }
  }

  companion object {
    const val REQUEST_MAPPING_TRAFFIC_SPAN = "/span"
    const val REQUEST_MAPPING_TRAFFIC_TRACE = "/trace"
  }
}
