package com.hookiesolutions.webhookie.audit.domain

import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/3/21 18:21
 */
enum class TrafficStatus {
  PROCESSING,
  NO_SUBSCRIPTION
}

data class TrafficStatusUpdate (
  val status: TrafficStatus,
  val time: Instant
)
