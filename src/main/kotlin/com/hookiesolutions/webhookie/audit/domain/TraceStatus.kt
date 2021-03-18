package com.hookiesolutions.webhookie.audit.domain

import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/3/21 18:21
 */
enum class TraceStatus {
  PROCESSING,
  NO_SUBSCRIPTION,
  ISSUES,
  OK
}

data class TraceStatusUpdate (
  val status: TraceStatus,
  val time: Instant
) {
  companion object {
    fun ok(at: Instant) = TraceStatusUpdate(TraceStatus.OK, at)
  }
}
