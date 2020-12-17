package com.hookiesolutions.webhookie.common.message.publisher

import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/12/20 23:49
 */
data class PublisherResponseErrorMessage(
  override val subscriptionMessage: SubscriptionMessage,
  override val status: HttpStatus,
  override val response: ByteArray,
  override val headers: HttpHeaders,
  override val reason: String
): PublisherServerMessage, PublisherErrorMessage {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is PublisherResponseErrorMessage) return false

    if (status != other.status) return false
    if (!response.contentEquals(other.response)) return false
    if (headers != other.headers) return false
    if (reason != other.reason) return false

    return true
  }

  override fun hashCode(): Int {
    var result = status.hashCode()
    result = 31 * result + response.contentHashCode()
    result = 31 * result + headers.hashCode()
    result = 31 * result + reason.hashCode()
    return result
  }

}
