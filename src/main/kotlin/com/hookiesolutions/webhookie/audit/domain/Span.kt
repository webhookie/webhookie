package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_ID
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.SPAN_COLLECTION_NAME
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails
import com.hookiesolutions.webhookie.common.model.dto.CallbackDTO
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/3/21 15:35
 */

@Document(collection = SPAN_COLLECTION_NAME)
@TypeAlias("span")
data class Span(
  @Indexed(name = "span_traceId")
  val traceId: String,
  @Indexed(name = "span_spanId", unique = true)
  val spanId: String,
  val application: ApplicationDetails,
  val callback: CallbackDTO,
  val statusUpdate: SpanStatusUpdate,
  val statusHistory: List<SpanStatusUpdate> = emptyList()
): AbstractEntity() {
  class Queries {
    companion object {
      fun bySpanId(spanId: String): Criteria {
        return where(KEY_SPAN_ID).`is`(spanId)
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_SPAN_ID = "spanId"
      const val SPAN_COLLECTION_NAME = "span"
    }
  }

  class Builder {
    private lateinit var traceId: String
    private lateinit var spanId: String
    private lateinit var application: ApplicationDetails
    private lateinit var callback: CallbackDTO
    private var status: SpanStatus = SpanStatus.PROCESSING
    private lateinit var time: Instant

    fun message(message: SignableSubscriptionMessage) = apply {
      this.traceId = message.traceId
      this.spanId = message.spanId
      this.application = message.subscription.application
      this.callback = message.subscription.callback
    }
    fun traceId(traceId: String) = apply { this.traceId = traceId }
    fun spanId(spanId: String) = apply { this.spanId = spanId }
    fun time(time: Instant) = apply { this.time = time }
    fun application(application: ApplicationDetails) = apply { this.application = application }
    fun callback(callback: CallbackDTO) = apply { this.callback = callback }
    fun status(status: SpanStatus) = apply { this.status = status }

    fun build(): Span {
      val statusUpdate = SpanStatusUpdate(status, time)
      return Span(
        traceId = traceId,
        spanId = spanId,
        application,
        callback,
        statusUpdate,
        listOf(statusUpdate)
      )
    }
  }
}
