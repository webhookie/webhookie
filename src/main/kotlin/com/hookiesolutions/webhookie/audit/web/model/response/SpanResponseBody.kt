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
