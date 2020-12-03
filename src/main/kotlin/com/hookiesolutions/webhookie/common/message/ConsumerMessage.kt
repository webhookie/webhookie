package com.hookiesolutions.webhookie.common.message

import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.HEADER_CONTENT_TYPE
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TOPIC
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
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
  private val message: Message<*>
) {
  val payload: Any
    get() = message.payload

  val headers: MessageHeaders
    get() = message.headers

  companion object {
    fun from(message: Message<*>): ConsumerMessage {
      val topic = message.headers[WH_HEADER_TOPIC] as String
      val traceId = message.headers[WH_HEADER_TRACE_ID] as String
      val contentType = message.headers[HEADER_CONTENT_TYPE] as String
      return ConsumerMessage(topic, traceId, contentType, message)
    }
  }
}