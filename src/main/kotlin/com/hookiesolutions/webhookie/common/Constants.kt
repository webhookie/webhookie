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
        const val WH_HEADER_TRACE_ID_MISSING = "wh-trace-id-missing"
        const val WH_HEADER_SPAN_ID = "wh-span-id"
        const val WH_HEADER_UNBLOCKED = "wh-unblocked"
        const val WH_HEADER_RESENT = "wh-resent"
        const val WH_HEADER_REQUESTED_BY = "wh-requested-by"
        const val WH_HEADER_AUTHORIZED_SUBSCRIBER = "wh-authorized-subscriber"
        const val HEADER_CONTENT_TYPE = "content_type"
        const val WH_HEADER_SEQUENCE_SIZE = "wh-sequence-size"

        val WH_REQUIRED_HEADERS = setOf(
          WH_HEADER_TOPIC,
          HEADER_CONTENT_TYPE
        )

        val WH_ALL_HEADERS = setOf(
          WH_HEADER_TOPIC,
          WH_HEADER_TRACE_ID,
          WH_HEADER_SPAN_ID,
          WH_HEADER_UNBLOCKED,
          WH_HEADER_RESENT,
          WH_HEADER_REQUESTED_BY,
          WH_HEADER_AUTHORIZED_SUBSCRIBER,
          HEADER_CONTENT_TYPE,
          WH_HEADER_SEQUENCE_SIZE
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
        const val DELAYED_SUBSCRIPTION_CHANNEL_NAME = "delaySubscriptionChannel"
        const val SUBSCRIPTION_ERROR_CHANNEL_NAME = "subscriptionErrorChannel"
        const val BLOCKED_SUBSCRIPTION_CHANNEL_NAME = "blockedSubscriptionChannel"
        const val SUBSCRIPTION_ACTIVATED_CHANNEL_NAME = "subscriptionActivatedChannel"
        const val SUBSCRIPTION_DEACTIVATED_CHANNEL_NAME = "subscriptionDeactivatedChannel"
        const val NO_SUBSCRIPTION_CHANNEL_NAME = "noSubscriptionChannel"
      }
    }

    class Publisher {
      companion object {
        const val PUBLISHER_SUCCESS_CHANNEL = "publisherSuccessChannel"
        const val PUBLISHER_RESPONSE_ERROR_CHANNEL = "publisherResponseErrorChannel"
        const val PUBLISHER_REQUEST_ERROR_CHANNEL = "publisherRequestErrorChannel"
        const val PUBLISHER_OTHER_ERROR_CHANNEL = "publisherOtherErrorChannel"
        const val RETRYABLE_PUBLISHER_ERROR_CHANNEL = "retryablePublisherErrorChannel"
      }
    }

    class Admin {
      companion object {
        const val ADMIN_CONSUMER_GROUP_DELETED_CHANNEL_NAME = "consumerGroupHasBeenDeletedChannel"
        const val ADMIN_CONSUMER_GROUP_UPDATED_CHANNEL_NAME = "consumerGroupHasBeenUpdatedChannel"
        const val ADMIN_PROVIDER_GROUP_DELETED_CHANNEL_NAME = "providerGroupHasBeenDeletedChannel"
        const val ADMIN_PROVIDER_GROUP_UPDATED_CHANNEL_NAME = "providerGroupHasBeenUpdatedChannel"
      }
    }
  }

  class Security {
    class Roles {
      companion object {
        const val ROLE_CONSUMER: String = "WH_CONSUMER"
        const val ROLE_PROVIDER: String = "WH_PROVIDER"
        const val ROLE_ADMIN: String = "WH_ADMIN"
      }
    }
  }

  companion object {
    const val DEFAULT_CONSUMER_GROUP = "Default"
  }
}
