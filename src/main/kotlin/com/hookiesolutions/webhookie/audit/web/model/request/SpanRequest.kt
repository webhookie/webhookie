package com.hookiesolutions.webhookie.audit.web.model.request

import com.hookiesolutions.webhookie.audit.domain.SpanStatus
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/3/21 14:40
 */
data class SpanRequest(
  val traceId: String? = null,
  val spanId: String? = null,
  val topic: String? = null,
  val application: String? = null,
  val entity: String? = null,
  val callback: String? = null,
  val from: Instant? = null,
  val to: Instant? = null,
  val status: List<SpanStatus> = emptyList()
) {
  class Builder {
    private var traceId: String? = null
    private var spanId: String? = null
    private var topic: String? = null
    private var application: String? = null
    private var entity: String? = null
    private var callback: String? = null
    private var status: List<SpanStatus> = emptyList()
    private var from: Instant? = null
    private var to: Instant? = null

    fun traceId(traceId: String?) = apply { this.traceId = traceId }
    fun spanId(spanId: String?) = apply { this.spanId = spanId }
    fun topic(topic: String?) = apply { this.topic = topic }
    fun application(application: String?) = apply { this.application = application }
    fun entity(entity: String?) = apply { this.entity = entity }
    fun callback(callback: String?) = apply { this.callback = callback }
    fun status(status: List<SpanStatus>) = apply { this.status = status }
    fun from(from: Instant?) = apply { this.from = from }
    fun to(to: Instant?) = apply { this.to = to }

    fun build(): SpanRequest {
      return SpanRequest(
        traceId = traceId,
        spanId = spanId,
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