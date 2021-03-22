package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_LAST_STATUS
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_ID
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SPAN_TOPIC
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.KEY_SUBSCRIPTION
import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.SPAN_COLLECTION_NAME
import com.hookiesolutions.webhookie.audit.domain.SpanStatusUpdate.Keys.Companion.KEY_STATUS
import com.hookiesolutions.webhookie.audit.domain.SpanStatusUpdate.Keys.Companion.KEY_TIME
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails.Keys.Companion.KEY_APPLICATION_ENTITY
import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails.Keys.Companion.KEY_APPLICATION_ID
import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails.Keys.Companion.KEY_APPLICATION_NAME
import com.hookiesolutions.webhookie.common.model.dto.CallbackDTO.Keys.Companion.KEY_URL
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDetails
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDetails.Keys.Companion.KEY_APPLICATION
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDetails.Keys.Companion.KEY_CALLBACK
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDetails.Keys.Companion.KEY_TOPIC
import com.hookiesolutions.webhookie.common.repository.GenericRepository.Companion.fieldName
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
  val subscription: SubscriptionDetails,
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

      fun applicationsIn(ids: Collection<String>): Criteria {
        return where("$KEY_SUBSCRIPTION.$KEY_APPLICATION.$KEY_APPLICATION_ID").`in`(ids)
      }

      fun spanTopicIn(topics: List<String>): Criteria {
        return where(KEY_SPAN_TOPIC).`in`(topics)
      }

      fun statusIn(statusList: List<SpanStatus>): Criteria {
        return where("$KEY_LAST_STATUS.$KEY_STATUS").`in`(statusList)
      }

      fun spanIsAfter(from: Instant): Criteria {
        return where(fieldName(KEY_LAST_STATUS, KEY_TIME)).gte(from)
      }

      fun spanIsBefore(from: Instant): Criteria {
        return where(fieldName(KEY_LAST_STATUS, KEY_TIME)).lte(from)
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_TRACE_ID = "traceId"
      const val KEY_SPAN_ID = "spanId"
      const val SPAN_COLLECTION_NAME = "span"
      const val KEY_SUBSCRIPTION = "subscription"
      const val KEY_STATUS_HISTORY = "statusHistory"
      const val KEY_LAST_STATUS = "lastStatus"
      const val KEY_NEXT_RETRY = "nextRetry"
      const val KEY_RETRY_HISTORY = "retryHistory"
      const val KEY_LATEST_RESULT = "latestResult"

      val KEY_SPAN_TOPIC = fieldName(KEY_SUBSCRIPTION, KEY_TOPIC)
      val KEY_SPAN_APPLICATION = fieldName(KEY_SUBSCRIPTION, KEY_APPLICATION, KEY_APPLICATION_NAME)
      val KEY_SPAN_ENTITY = fieldName(KEY_SUBSCRIPTION, KEY_APPLICATION, KEY_APPLICATION_ENTITY)
      val KEY_SPAN_CALLBACK = fieldName(KEY_SUBSCRIPTION, KEY_CALLBACK, KEY_URL)
    }
  }

  class Builder {
    private lateinit var traceId: String
    private lateinit var spanId: String
    private lateinit var subscription: SubscriptionDTO
    private var status: SpanStatus = SpanStatus.PROCESSING
    private lateinit var time: Instant

    fun message(message: SignableSubscriptionMessage) = apply {
      this.traceId = message.traceId
      this.spanId = message.spanId
      this.subscription = message.subscription
    }

    fun message(message: BlockedSubscriptionMessageDTO) = apply {
      this.traceId = message.traceId
      this.spanId = message.spanId
      this.subscription = message.subscription
    }

    fun traceId(traceId: String) = apply { this.traceId = traceId }
    fun spanId(spanId: String) = apply { this.spanId = spanId }
    fun time(time: Instant) = apply { this.time = time }
    fun status(status: SpanStatus) = apply { this.status = status }

    fun build(): Span {
      val statusUpdate = SpanStatusUpdate(status, time)
      return Span(
        traceId = traceId,
        spanId = spanId,
        subscription = SubscriptionDetails.from(subscription),
        lastStatus = statusUpdate,
        statusHistory = listOf(statusUpdate)
      )
    }
  }
}

