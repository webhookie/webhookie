package com.hookiesolutions.webhookie.audit.web.model.response

import com.hookiesolutions.webhookie.audit.domain.Span
import org.springframework.http.HttpHeaders
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/5/21 13:17
 */
data class SpanResponseBody(
  val spanId: String,
  val time: Instant,
  val statusCode: Int,
  val contentType: String,
  val body: String,
  val headers: HttpHeaders,
  val retryNo: Int
) {
  companion object {
    private const val UNKNOWN_CONTENT_TYPE = "UNKNOWN"
    private const val UNKNOWN_BODY = "s"
    private const val UNKNOWN_STATUS_CODE = -100
    private const val UNKNOWN_RETRY_NO = -1

    fun from(span: Span): Mono<SpanResponseBody> {
      val response = span.latestResult ?: return Mono.empty()
      val contentType = if (response.headers.contentType != null) {
        response.headers.contentType.toString()
      } else {
        UNKNOWN_CONTENT_TYPE
      }
      return SpanResponseBody(
          span.spanId,
          response.time,
          response.statusCode,
          contentType,
          response.body,
          response.headers,
          response.retryNo
        ).toMono()
    }

    fun notReady(spanId: String, at: Instant): Mono<SpanResponseBody> {
      return SpanResponseBody(
        spanId,
        at,
        UNKNOWN_STATUS_CODE,
        UNKNOWN_CONTENT_TYPE,
        UNKNOWN_BODY,
        HttpHeaders(),
        UNKNOWN_RETRY_NO
      ).toMono()
    }
  }
}
