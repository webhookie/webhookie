package com.hookiesolutions.webhookie.audit.domain

import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/3/21 15:39
 */
enum class SpanStatus {
  PROCESSING,
  RETRYING,
  BLOCKED,
  NOT_OK,
  OK
}

data class SpanStatusUpdate(
  val status: SpanStatus,
  val time: Instant
) {
  companion object {
    fun ok(at: Instant): SpanStatusUpdate = SpanStatusUpdate(SpanStatus.OK, at)
    fun notOk(at: Instant): SpanStatusUpdate = SpanStatusUpdate(SpanStatus.NOT_OK, at)
    fun blocked(at: Instant): SpanStatusUpdate = SpanStatusUpdate(SpanStatus.BLOCKED, at)
    fun retrying(at: Instant): SpanStatusUpdate = SpanStatusUpdate(SpanStatus.RETRYING, at)
  }
}
