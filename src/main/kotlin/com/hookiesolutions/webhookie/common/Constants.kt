package com.hookiesolutions.webhookie.common

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/12/20 13:40
 */
class Constants {
  class Queue {
    class Headers {
      companion object {
        const val WH_HEADER_TOPIC = "wh-topic"
        const val WH_HEADER_TRACE_ID = "wh-trace-id"
        const val WH_HEADER_AUTHORIZED_SUBSCRIBER = "wh-authorized-subscriber"
        const val HEADER_CONTENT_TYPE = "content_type"

        val WH_REQUIRED_HEADERS = setOf(
          WH_HEADER_TOPIC,
          WH_HEADER_TRACE_ID,
          HEADER_CONTENT_TYPE
        )
      }
    }
  }
}