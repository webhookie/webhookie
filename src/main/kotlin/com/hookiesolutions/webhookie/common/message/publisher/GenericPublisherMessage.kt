package com.hookiesolutions.webhookie.common.message.publisher

import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/12/20 23:46
 */
interface GenericPublisherMessage {
  val subscriptionMessage: SubscriptionMessage
  companion object {
    fun success(subscriptionMessage: SubscriptionMessage, response: ResponseEntity<ByteArray>): GenericPublisherMessage {
      return PublisherSuccessMessage(subscriptionMessage, response.statusCode, response.body ?: ByteArray(0), response.headers)
    }

    fun responseError(subscriptionMessage: SubscriptionMessage, throwable: WebClientResponseException): GenericPublisherMessage {
      return PublisherResponseErrorMessage(subscriptionMessage, throwable.statusCode, throwable.responseBodyAsByteArray, throwable.headers, throwable.localizedMessage)
    }

    fun requestError(subscriptionMessage: SubscriptionMessage, throwable: WebClientRequestException): GenericPublisherMessage {
      return PublisherRequestErrorMessage(subscriptionMessage, throwable.localizedMessage, throwable.headers)
    }

    fun unknownError(subscriptionMessage: SubscriptionMessage, throwable: Throwable): GenericPublisherMessage {
      return PublisherOtherErrorMessage(subscriptionMessage, throwable.localizedMessage)
    }
  }
}