package com.hookiesolutions.webhookie.common.message.publisher

import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/12/20 23:46
 */
interface GenericPublisherMessage {
  val subscriptionMessage: SignableSubscriptionMessage

  val spanId: String
    get() = subscriptionMessage.spanId

  val traceId: String
    get() = subscriptionMessage.traceId

  val url: String
    get() = subscriptionMessage.subscription.callback.url

  companion object {
    fun success(subscriptionMessage: SignableSubscriptionMessage, response: ResponseEntity<ByteArray>): GenericPublisherMessage {
      return PublisherSuccessMessage(subscriptionMessage, ServerResponse(response.statusCode, response.body ?: ByteArray(0), response.headers))
    }

    fun responseError(subscriptionMessage: SignableSubscriptionMessage, throwable: WebClientResponseException): GenericPublisherMessage {
      return PublisherResponseErrorMessage(subscriptionMessage, ServerResponse(throwable.statusCode, throwable.responseBodyAsByteArray, throwable.headers), throwable.localizedMessage)
    }

    fun requestError(subscriptionMessage: SignableSubscriptionMessage, throwable: WebClientRequestException): GenericPublisherMessage {
      return PublisherRequestErrorMessage(subscriptionMessage, throwable.localizedMessage, throwable.headers)
    }

    fun unknownError(subscriptionMessage: SignableSubscriptionMessage, throwable: Throwable): GenericPublisherMessage {
      return PublisherOtherErrorMessage(subscriptionMessage, throwable.localizedMessage)
    }
  }
}
