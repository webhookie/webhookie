package com.hookiesolutions.webhookie.subscription.domain

import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 11/2/21 14:31
 */
data class StatusUpdate(
  val status: SubscriptionStatus,
  val reason: String?,
  val time: Instant
) {
  class Builder {
    private lateinit var status: SubscriptionStatus
    private var reason: String? = null
    private lateinit var time: Instant

    fun status(status: SubscriptionStatus) = apply { this.status = status }
    fun reason(reason: String?) = apply { this.reason = reason }
    fun at(time: Instant) = apply { this.time = time }

    fun build(): StatusUpdate = StatusUpdate(status, reason, time)
  }

  companion object {
    fun saved(at: Instant): StatusUpdate {
      return Builder()
        .status(SubscriptionStatus.SAVED)
        .at(at)
        .build()
    }

    fun validated(at: Instant): StatusUpdate {
      return Builder()
        .status(SubscriptionStatus.VALIDATED)
        .at(at)
        .build()
    }

    fun activated(at: Instant): StatusUpdate {
      return Builder()
        .status(SubscriptionStatus.ACTIVATED)
        .at(at)
        .build()
    }
  }
}