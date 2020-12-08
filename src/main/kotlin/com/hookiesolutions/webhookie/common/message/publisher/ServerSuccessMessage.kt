package com.hookiesolutions.webhookie.common.message.publisher

import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/12/20 23:48
 */
data class ServerSuccessMessage(
  override val subscriptionMessage: SubscriptionMessage,
  override val status: HttpStatus,
  override val response: ByteArray,
  override val headers: HttpHeaders
): PublisherServerMessage {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ServerSuccessMessage) return false

    if (status != other.status) return false
    if (!response.contentEquals(other.response)) return false
    if (headers != other.headers) return false

    return true
  }

  override fun hashCode(): Int {
    var result = status.hashCode()
    result = 31 * result + response.contentHashCode()
    result = 31 * result + headers.hashCode()
    return result
  }
}
