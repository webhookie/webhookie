package com.hookiesolutions.webhookie.common.message

import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.HEADER_CONTENT_TYPE
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_AUTHORIZED_SUBSCRIBER
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TOPIC
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
import org.springframework.data.annotation.Transient
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.messaging.Message

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 12:31
 */
data class ConsumerMessage(
  val traceId: String,
  val topic: String,
  val contentType: String,
  val authorizedSubscribers: Set<String>,
  val payload: ByteArray,
  val headers: Map<String, Any>,
) {
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

  companion object {
    fun from(message: Message<ByteArray>): ConsumerMessage {
      val topic = message.headers[WH_HEADER_TOPIC] as String
      val traceId = message.headers[WH_HEADER_TRACE_ID] as String
      val contentType = message.headers[HEADER_CONTENT_TYPE] as String
      @Suppress("UNCHECKED_CAST")
      val authorizedSubscribers: Collection<String> = message.headers[WH_HEADER_AUTHORIZED_SUBSCRIBER] as? Collection<String> ?: emptySet()

      return ConsumerMessage(
        traceId,
        topic,
        contentType,
        authorizedSubscribers.toSet(),
        message.payload,
        message.headers
      )
    }
  }

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
