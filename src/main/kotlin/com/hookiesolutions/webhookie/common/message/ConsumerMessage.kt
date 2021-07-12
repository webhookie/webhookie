package com.hookiesolutions.webhookie.common.message

import org.springframework.data.annotation.Transient
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 12:31
 */
data class ConsumerMessage(
  override val traceId: String,
  val topic: String,
  val contentType: String,
  val authorizedSubscribers: Set<String>,
  val payload: ByteArray,
  val headers: Map<String, Any>,
): WebhookieMessage {
  fun addMessageHeaders(headers: HttpHeaders) {
    this.headers
      .forEach {
        val stringValue = it.value as? String
        if (stringValue != null) {
          headers.addIfAbsent(it.key, stringValue)
        } else {
          @Suppress("UNCHECKED_CAST") val listValue = it.value as? List<String>
          if(listValue != null) {
            headers.addAll(it.key, listValue)
          }
        }
      }
  }

  @Transient
  fun  mediaType(): MediaType = MediaType.parseMediaType(contentType)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ConsumerMessage) return false

    if (traceId != other.traceId) return false
    if (topic != other.topic) return false
    if (contentType != other.contentType) return false
    if (authorizedSubscribers != other.authorizedSubscribers) return false
    if (!payload.contentEquals(other.payload)) return false
    if (headers != other.headers) return false

    return true
  }

  override fun hashCode(): Int {
    var result = traceId.hashCode()
    result = 31 * result + topic.hashCode()
    result = 31 * result + contentType.hashCode()
    result = 31 * result + authorizedSubscribers.hashCode()
    result = 31 * result + payload.contentHashCode()
    result = 31 * result + headers.hashCode()
    return result
  }
}
