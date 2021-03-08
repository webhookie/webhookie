package com.hookiesolutions.webhookie.audit.domain

import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/3/21 15:39
 */
enum class SpanStatus {
  PROCESSING,
  RETRYING
}

data class SpanStatusUpdate(
  val status: SpanStatus,
  val time: Instant
)
