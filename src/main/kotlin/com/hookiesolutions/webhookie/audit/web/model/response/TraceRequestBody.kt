package com.hookiesolutions.webhookie.audit.web.model.response

import com.hookiesolutions.webhookie.audit.domain.Trace

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/5/21 13:38
 */
data class TraceRequestBody(
  val traceId: String,
  val contentType: String,
  val payload: String,
  val headers: Map<String, Any>,
  ) {
  companion object {
    fun from(trace: Trace): TraceRequestBody {
      val consumerMessage = trace.consumerMessage
      return TraceRequestBody(
        trace.traceId,
        consumerMessage.contentType,
        consumerMessage.payload.decodeToString(),
        consumerMessage.headers
      )
    }
  }
}
