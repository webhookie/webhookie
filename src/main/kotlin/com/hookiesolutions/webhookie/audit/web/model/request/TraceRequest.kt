package com.hookiesolutions.webhookie.audit.web.model.request

import com.hookiesolutions.webhookie.audit.domain.TraceStatus
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/3/21 14:40
 */
data class TraceRequest(
  val traceId: String? = null,
  val topic: String? = null,
  val application: String? = null,
  val entity: String? = null,
  val callback: String? = null,
  val from: Instant? = null,
  val to: Instant? = null,
  val status: List<TraceStatus> = emptyList()
) {
  class Builder {
    private var traceId: String? = null
    private var topic: String? = null
    private var application: String? = null
    private var entity: String? = null
    private var callback: String? = null
    private var status: List<TraceStatus> = emptyList()
    private var from: Instant? = null
    private var to: Instant? = null

    fun traceId(traceId: String?) = apply { this.traceId = traceId }
    fun topic(topic: String?) = apply { this.topic = topic }
    fun application(application: String?) = apply { this.application = application }
    fun entity(entity: String?) = apply { this.entity = entity }
    fun callback(callback: String?) = apply { this.callback = callback }
    fun status(status: List<TraceStatus>) = apply { this.status = status }
    fun from(from: Instant?) = apply { this.from = from }
    fun to(to: Instant?) = apply { this.to = to }

    fun build(): TraceRequest {
      return TraceRequest(
        traceId = traceId,
        topic = topic,
        application = application,
        entity = entity,
        callback = callback,
        from = from,
        to = to,
        status = status
      )
    }
  }
}
