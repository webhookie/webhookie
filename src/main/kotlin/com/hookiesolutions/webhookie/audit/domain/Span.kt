package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_ID
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.SPAN_COLLECTION_NAME
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails
import com.hookiesolutions.webhookie.common.model.dto.CallbackDTO
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.http.HttpHeaders
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
  val lastStatus: SpanStatusUpdate,
  val statusHistory: List<SpanStatusUpdate> = emptyList(),
  val nextRetry: SpanRetry? = null,
  val retryHistory: Set<SpanRetry> = emptySet(),
  val latestResult: SpanResult? = null
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
      const val KEY_STATUS_HISTORY = "statusHistory"
      const val KEY_LAST_STATUS = "lastStatus"
      const val KEY_NEXT_RETRY = "nextRetry"
      const val KEY_RETRY_HISTORY = "retryHistory"
      const val KEY_LATEST_RESULT = "latestResult"
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

    fun message(message: BlockedSubscriptionMessageDTO) = apply {
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

data class SpanRetry (
  val time: Instant,
  val no: Int,
  val statusCode: Int? = null
) {
  companion object {
    const val KEY_RETRY_NO = "no"
    const val KEY_RETRY_STATUS_CODE = "statusCode"
  }
}

data class SpanResult (
  val time: Instant,
  val statusCode: Int,
  val body: String,
  val headers: HttpHeaders,
  val retryNo: Int
) {
  class Builder {
    private lateinit var time: Instant
    private var statusCode: Int = -1
    private lateinit var body: String
    private lateinit var headers: HttpHeaders
    private var retryNo: Int = -1

    fun time(time: Instant) = apply { this.time = time }

    fun message(message: PublisherSuccessMessage) = apply {
      this.statusCode = message.response.status.value()
      this.body = message.response.body()
      this.headers = message.response.headers
      this.retryNo = message.subscriptionMessage.numberOfRetries
    }

    fun message(message: PublisherRequestErrorMessage) = apply {
      this.statusCode = -1
      this.body = message.reason
      this.headers = message.headers
      this.retryNo = message.subscriptionMessage.numberOfRetries
    }

    fun message(message: PublisherResponseErrorMessage) = apply {
      this.statusCode = message.response.status.value()
      this.body = message.response.body()
      this.headers = message.response.headers
      this.retryNo = message.subscriptionMessage.numberOfRetries
    }

    fun build(): SpanResult {
      return SpanResult(time, statusCode, body, headers, retryNo)
    }
  }
}
