package com.hookiesolutions.webhookie.common.message.subscription

import com.hookiesolutions.webhookie.common.message.WebhookieHeaders
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
data class BlockedSubscriptionMessageDTO(
  val id: String?,
  val headers: WebhookieHeaders,
  val originalSpanId: String,
  val payload: ByteArray,
  val messageHeaders: Map<String, Any>,
  val subscription: SubscriptionDTO,
  val blockedDetails: StatusUpdate
) {
  val topic: String
    get() = headers.topic

  val traceId: String
    get() = headers.traceId

  val spanId: String
    get() = originalSpanId

  @Suppress("DuplicatedCode")
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BlockedSubscriptionMessageDTO) return false

    if (id != other.id) return false
    if (headers != other.headers) return false
    if (originalSpanId != other.originalSpanId) return false
    if (!payload.contentEquals(other.payload)) return false
    if (messageHeaders != other.messageHeaders) return false
    if (subscription != other.subscription) return false
    if (blockedDetails != other.blockedDetails) return false

    return true
  }

  @Suppress("DuplicatedCode")
  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + headers.hashCode()
    result = 31 * result + originalSpanId.hashCode()
    result = 31 * result + payload.contentHashCode()
    result = 31 * result + messageHeaders.hashCode()
    result = 31 * result + subscription.hashCode()
    result = 31 * result + blockedDetails.hashCode()
    return result
  }
}
