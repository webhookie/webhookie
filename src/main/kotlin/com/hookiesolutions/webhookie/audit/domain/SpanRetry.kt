package com.hookiesolutions.webhookie.audit.domain

import java.time.Instant

data class SpanRetry (
  val time: Instant,
  val no: Int,
  val retryNo: Int,
  val sentBy: String,
  val reason: SpanSendReason,
  val statusCode: Int? = null
) {
  companion object {
    const val KEY_RETRY_NO = "no"
    const val KEY_RETRY_STATUS_CODE = "statusCode"

    const val SENT_BY_WEBHOOKIE = "Webhookie"
  }
}

enum class SpanSendReason {
  SEND,
  RETRY,
  RESEND,
  UNBLOCK
}
