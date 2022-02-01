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

package com.hookiesolutions.webhookie.common.model.dto

import com.hookiesolutions.webhookie.common.extension.capitalize
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
  fun capitalizedName(): String {
    return status.capitalize()
  }

  class Keys {
    companion object {
      const val KEY_STATUS = "status"
      const val KEY_TIME = "time"
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
    fun updateStatus(at: Instant, status: SubscriptionStatus, reason: String? = null): StatusUpdate {
      return Builder()
        .status(status)
        .reason(reason)
        .at(at)
        .build()
    }

    fun draft(at: Instant): StatusUpdate {
      return updateStatus(at, SubscriptionStatus.DRAFT)
    }

    fun activated(at: Instant): StatusUpdate {
      return updateStatus(at, SubscriptionStatus.ACTIVATED)
    }

    fun submitted(at: Instant): StatusUpdate {
      return updateStatus(at, SubscriptionStatus.SUBMITTED)
    }

    fun deactivated(at: Instant, reason: String?): StatusUpdate {
      return updateStatus(at, SubscriptionStatus.DEACTIVATED, reason)
    }

    fun suspended(at: Instant, reason: String?): StatusUpdate {
      return updateStatus(at, SubscriptionStatus.SUSPENDED, reason)
    }

    fun blocked(at: Instant, reason: String?): StatusUpdate {
      return updateStatus(at, SubscriptionStatus.BLOCKED, reason)
    }
  }
}
