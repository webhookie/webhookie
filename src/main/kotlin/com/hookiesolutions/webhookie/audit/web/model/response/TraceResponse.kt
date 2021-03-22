package com.hookiesolutions.webhookie.audit.web.model.response

import com.hookiesolutions.webhookie.audit.domain.Trace
import com.hookiesolutions.webhookie.audit.domain.TraceStatusUpdate

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 22/3/21 14:05
 */
data class TraceResponse(
  val traceId: String,
  val topic: String,
  val status: TraceStatusUpdate,
  val authorizedSubscribers: Set<String>,
) {
  companion object {
    fun from(trace: Trace): TraceResponse {
      return TraceResponse(
        trace.traceId,
        trace.topic,
        trace.statusUpdate,
        trace.consumerMessage.authorizedSubscribers
      )
    }
  }
}
