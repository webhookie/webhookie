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
  val applicationId: String? = null,
  val entity: String? = null,
  val callbackId: String? = null,
  val from: Instant? = null,
  val to: Instant? = null,
  val status: List<TraceStatus> = emptyList()
) {
  class Builder {
    private var traceId: String? = null
    private var topic: String? = null
    private var applicationId: String? = null
    private var entity: String? = null
    private var callbackId: String? = null
    private var status: List<TraceStatus> = emptyList()
    private var from: Instant? = null
    private var to: Instant? = null

    fun traceId(traceId: String?) = apply { this.traceId = traceId }
    fun topic(topic: String?) = apply { this.topic = topic }
    fun applicationId(applicationId: String?) = apply { this.applicationId = applicationId }
    fun entity(entity: String?) = apply { this.entity = entity }
    fun callbackId(callbackId: String?) = apply { this.callbackId = callbackId }
    fun status(status: List<TraceStatus>) = apply { this.status = status }
    fun from(from: Instant?) = apply { this.from = from }
    fun to(to: Instant?) = apply { this.to = to }

    fun build(): TraceRequest {
      return TraceRequest(
        traceId = traceId,
        topic = topic,
        applicationId = applicationId,
        entity = entity,
        callbackId = callbackId,
        from = from,
        to = to,
        status = status
      )
    }
  }
}
