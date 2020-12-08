package com.hookiesolutions.webhookie.common.message

import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.HEADER_CONTENT_TYPE
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_AUTHORIZED_SUBSCRIBER
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TOPIC
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 12:31
 */
data class ConsumerMessage(
  val topic: String,
  val traceId: String,
  val contentType: String,
  val authorizedSubscribers: Set<String> = emptySet(),
  val message: Message<ByteArray>
) {
  val payload: ByteArray
    get() = message.payload

  @Suppress("unused")
  val headers: MessageHeaders
    get() = message.headers

  fun addMessageHeaders(headers: HttpHeaders) {
    message.headers
      .forEach {
        val stringValue = it.value as? String
        @Suppress("UNCHECKED_CAST") val listValue = it.value as? List<String>
        if (stringValue != null) {
          headers.addIfAbsent(it.key, stringValue)
        } else if(listValue != null) {
          headers.addAll(it.key, listValue)
        }
      }
  }

  val mediaType: MediaType
    get() = MediaType.parseMediaType(contentType)

  companion object {
    fun from(message: Message<ByteArray>): ConsumerMessage {
      val topic = message.headers[WH_HEADER_TOPIC] as String
      val traceId = message.headers[WH_HEADER_TRACE_ID] as String
      val contentType = message.headers[HEADER_CONTENT_TYPE] as String
      @Suppress("UNCHECKED_CAST")
      val authorizedSubscribers: Collection<String> = message.headers[WH_HEADER_AUTHORIZED_SUBSCRIBER] as? Collection<String> ?: emptySet()
      return ConsumerMessage(topic, traceId, contentType, authorizedSubscribers.toSet(), message)
    }
  }
}