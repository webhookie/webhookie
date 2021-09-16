/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.audit.web

import com.hookiesolutions.webhookie.audit.domain.*
import com.hookiesolutions.webhookie.audit.service.SpanService
import com.hookiesolutions.webhookie.audit.service.TraceService
import com.hookiesolutions.webhookie.audit.web.TrafficAPIDocs.Companion.REQUEST_MAPPING_TRAFFIC
import com.hookiesolutions.webhookie.audit.web.model.request.SpanRequest
import com.hookiesolutions.webhookie.audit.web.model.request.TraceRequest
import com.hookiesolutions.webhookie.audit.web.model.response.SpanResponse
import com.hookiesolutions.webhookie.audit.web.model.response.TraceRequestBody
import com.hookiesolutions.webhookie.audit.web.model.response.TraceResponse
import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
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
) {
  @GetMapping(
    REQUEST_MAPPING_TRAFFIC_SPAN,
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun userSpans(
    @RequestParam(required = false) subscriptionId: String?,
    @RequestParam(required = false) traceId: String?,
    @RequestParam(required = false) spanId: String?,
    @RequestParam(required = false) application: String?,
    @RequestParam(required = false) entity: String?,
    @RequestParam(required = false) callback: String?,
    @RequestParam(required = false) topic: String?,
    @RequestParam(required = false, defaultValue = "") status: List<SpanStatus>,
    @RequestParam(required = false) from: Instant?,
    @RequestParam(required = false) to: Instant?,
    @RequestParam(required = false) responseCode: Int?,
    pageable: Pageable
  ): Flux<SpanResponse> {
    val request = SpanRequest.Builder()
      .subscriptionId(subscriptionId)
      .traceId(traceId)
      .spanId(spanId)
      .application(application)
      .entity(entity)
      .callback(callback)
      .topic(topic)
      .status(status)
      .from(from)
      .to(to)
      .responseCode(responseCode)
      .build()
    return spanService.userSpans(pageable, request)
      .map { SpanResponse.from(it)}
  }

  @GetMapping(
    REQUEST_MAPPING_TRAFFIC_TRACE,
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun userTraces(
    @RequestParam(required = false) subscriptionId: String?,
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
      .subscriptionId(subscriptionId)
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
    @RequestParam(required = false) subscriptionId: String?,
    @RequestParam(required = false) applicationId: String?,
    @RequestParam(required = false) entity: String?,
    @RequestParam(required = false) callbackId: String?,
    pageable: Pageable
  ): Flux<SpanResponse> {
    val request = TraceRequest.Builder()
      .subscriptionId(subscriptionId)
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
  fun spanResponse(@PathVariable spanId: String): Mono<SpanResult> {
    return spanService.fetchSpanVerifyingReadAccess(spanId)
      .flatMap {
        Mono.justOrEmpty(it.latestResult)
      }
      .switchIfEmpty(Mono.error(EntityNotFoundException("Span Response is not ready yet!")))
  }

  @GetMapping(
    "$REQUEST_MAPPING_TRAFFIC_SPAN/{spanId}/request",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun spanRequest(@PathVariable spanId: String): Mono<SubscriptionRequest> {
    return spanService.fetchSpanVerifyingReadAccess(spanId)
      .map { it.nextRetry.request }
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
