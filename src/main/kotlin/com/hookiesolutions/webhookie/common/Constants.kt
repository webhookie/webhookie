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
        const val WH_HEADER_TRACE_SEQUENCE_SIZE = "wh-trace-sequence-size"

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
          WH_HEADER_TRACE_SEQUENCE_SIZE
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
        const val MISSING_SUBSCRIPTION_CHANNEL_NAME = "missingSubscriptionChannel"
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

    class Traffic {
      companion object {
        const val TRAFFIC_RESEND_SPAN_CHANNEL_NAME = "resendSpanChannel"
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
