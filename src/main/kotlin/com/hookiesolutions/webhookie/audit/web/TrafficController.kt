package com.hookiesolutions.webhookie.audit.web

import com.hookiesolutions.webhookie.audit.domain.SpanStatus
import com.hookiesolutions.webhookie.audit.domain.TraceStatus
import com.hookiesolutions.webhookie.audit.service.SpanService
import com.hookiesolutions.webhookie.audit.service.TraceService
import com.hookiesolutions.webhookie.audit.web.TrafficAPIDocs.Companion.REQUEST_MAPPING_TRAFFIC
import com.hookiesolutions.webhookie.audit.web.model.request.SpanRequest
import com.hookiesolutions.webhookie.audit.web.model.request.TraceRequest
import com.hookiesolutions.webhookie.audit.web.model.response.SpanResponse
import com.hookiesolutions.webhookie.audit.web.model.response.TraceResponse
import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
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
  private val traceService: TraceService
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
    @RequestParam(required = false) application: String?,
    @RequestParam(required = false) entity: String?,
    @RequestParam(required = false) callback: String?,
    @RequestParam(required = false) topic: String?,
    @RequestParam(required = false, defaultValue = "") status: List<TraceStatus>,
    @RequestParam(required = false) from: Instant?,
    @RequestParam(required = false) to: Instant?,
    pageable: Pageable
  ): Flux<TraceResponse> {
    val request = TraceRequest.Builder()
      .traceId(traceId)
      .application(application)
      .entity(entity)
      .callback(callback)
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
    pageable: Pageable
  ): Flux<SpanResponse> {
    return spanService.traceSpans(pageable, traceId)
      .map { SpanResponse.from(it)}
  }

  companion object {
    const val REQUEST_MAPPING_TRAFFIC_SPAN = "/span"
    const val REQUEST_MAPPING_TRAFFIC_TRACE = "/trace"
  }
}
