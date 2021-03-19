package com.hookiesolutions.webhookie.audit.web

import com.hookiesolutions.webhookie.audit.service.SpanService
import com.hookiesolutions.webhookie.audit.web.TrafficAPIDocs.Companion.REQUEST_MAPPING_TRAFFIC
import com.hookiesolutions.webhookie.audit.web.model.request.SpanRequest
import com.hookiesolutions.webhookie.audit.web.model.response.SpanResponse
import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/3/21 19:54
 */

@RestController
@SecurityRequirement(name = OAUTH2_SCHEME)
@RequestMapping(REQUEST_MAPPING_TRAFFIC)
class TrafficController(
  private val spanService: SpanService
) {
  @GetMapping(
    "/span",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun userSpans(
    @RequestParam(required = false) traceId: String?,
    @RequestParam(required = false) spanId: String?,
    @RequestParam(required = false) application: String?,
    @RequestParam(required = false) entity: String?,
    @RequestParam(required = false) callback: String?,
    @RequestParam(required = false) topic: String?,
    pageable: Pageable
  ): Flux<SpanResponse> {
    val request = SpanRequest.Builder()
      .traceId(traceId)
      .spanId(spanId)
      .application(application)
      .entity(entity)
      .callback(callback)
      .topic(topic)
      .build()
    return spanService.userSpans(pageable, request)
      .map { SpanResponse.from(it)}
  }
}
