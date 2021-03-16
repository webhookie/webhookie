package com.hookiesolutions.webhookie.common.model.dto

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
  class Keys {
    companion object {
      const val KEY_STATUS = "status"
    }
  }

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

    fun deactivated(at: Instant, reason: String?): StatusUpdate {
      return Builder()
        .status(SubscriptionStatus.DEACTIVATED)
        .reason(reason)
        .at(at)
        .build()
    }

    fun suspended(at: Instant, reason: String?): StatusUpdate {
      return Builder()
        .status(SubscriptionStatus.SUSPENDED)
        .reason(reason)
        .at(at)
        .build()
    }

    fun blocked(at: Instant, reason: String?): StatusUpdate {
      return Builder()
        .status(SubscriptionStatus.BLOCKED)
        .reason(reason)
        .at(at)
        .build()
    }
  }
}
