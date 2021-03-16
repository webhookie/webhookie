package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_STATUS_HISTORY
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_STATUS_UPDATE
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_TIME
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_TRACE_ID
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.TRACE_COLLECTION_NAME
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Update
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/3/21 18:17
 */
@Document(collection = TRACE_COLLECTION_NAME)
@TypeAlias("trace")
data class Trace(
  @Indexed(name = "trace_traceId", unique = true)
  val traceId: String,
  @Indexed(name = "trace_topic")
  val topic: String,
  val consumerMessage: ConsumerMessage,
  val time: Instant,
  val statusUpdate: TraceStatusUpdate,
  val statusHistory: List<TraceStatusUpdate> = emptyList()
): AbstractEntity() {

  class Queries {
    companion object {
      fun byTraceId(traceId: String): Criteria {
        return where(KEY_TRACE_ID).`is`(traceId)
      }
    }
  }

  class Updates {
    companion object {
      fun trafficStatusUpdate(statusUpdate: TraceStatusUpdate): Update {
        return Update()
          .set(KEY_STATUS_UPDATE, statusUpdate)
          .set(KEY_TIME, statusUpdate.time)
          .addToSet(KEY_STATUS_HISTORY, statusUpdate)
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_TRACE_ID = "traceId"
      const val KEY_STATUS_UPDATE = "statusUpdate"
      const val KEY_STATUS_HISTORY = "statusHistory"
      const val KEY_TIME = "time"
      const val TRACE_COLLECTION_NAME = "trace"
    }
  }

  class Builder {
    private lateinit var message: ConsumerMessage
    private lateinit var time: Instant
    private var status: TraceStatus = TraceStatus.PROCESSING

    fun message(message: ConsumerMessage) = apply { this.message = message }
    fun time(time: Instant) = apply { this.time = time }
    fun status(status: TraceStatus) = apply { this.status = status }

    fun build(): Trace {
      val statusUpdate = TraceStatusUpdate(status, time)
      return Trace(message.traceId, message.topic, message, time, statusUpdate, listOf(statusUpdate))
    }
  }

}
