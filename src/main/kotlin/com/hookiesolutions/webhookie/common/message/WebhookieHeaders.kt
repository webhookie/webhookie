package com.hookiesolutions.webhookie.common.message

import org.springframework.http.MediaType

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 16/12/20 23:36
 */

data class WebhookieHeaders(
  val topic: String,
  val traceId: String,
  val contentType: String,
  val authorizedSubscribers: Set<String> = emptySet()
) {
  val mediaType: MediaType
    get() = MediaType.parseMediaType(contentType)
}
