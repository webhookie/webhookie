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
        const val WH_HEADER_SPAN_ID = "wh-span-id"
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

  class Channels {
    class Consumer {
      companion object {
        const val CONSUMER_CHANNEL_NAME = "consumerChannel"
      }
    }

    class Subscription {
      companion object {
        const val SUBSCRIPTION_CHANNEL_NAME = "subscriptionChannel"
        const val BLOCKED_SUBSCRIPTION_CHANNEL_NAME = "blockedSubscriptionChannel"
        const val UNSUCCESSFUL_SUBSCRIPTION_CHANNEL_NAME = "unsuccessfulSubscriptionChannel"
        const val NO_SUBSCRIPTION_CHANNEL_NAME = "noSubscriptionChannel"
      }
    }

    class Publisher {
      companion object {
        const val PUBLISHER_SUCCESS_CHANNEL = "publisherSuccessChannel"
        const val PUBLISHER_RESPONSE_ERROR_CHANNEL = "publisherResponseErrorChannel"
        const val PUBLISHER_REQUEST_ERROR_CHANNEL = "publisherRequestErrorChannel"
        const val PUBLISHER_OTHER_ERROR_CHANNEL = "publisherOtherErrorChannel"
        const val RETRY_SUBSCRIPTION_MESSAGE_CHANNEL = "retrySubscriptionMessageChannel"
      }
    }
  }
}