package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Traffic.Keys.Companion.KEY_STATUS_HISTORY
import com.hookiesolutions.webhookie.audit.domain.Traffic.Keys.Companion.KEY_STATUS_UPDATE
import com.hookiesolutions.webhookie.audit.domain.Traffic.Keys.Companion.KEY_TIME
import com.hookiesolutions.webhookie.audit.domain.Traffic.Keys.Companion.KEY_TRACE_ID
import com.hookiesolutions.webhookie.audit.domain.Traffic.Keys.Companion.TRAFFIC_COLLECTION_NAME
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
@Document(collection = TRAFFIC_COLLECTION_NAME)
@TypeAlias("traffic")
data class Traffic(
  @Indexed(name = "traffic_traceId", unique = true)
  val traceId: String,
  @Indexed(name = "traffic_topic")
  val topic: String,
  val consumerMessage: ConsumerMessage,
  val time: Instant,
  val statusUpdate: TrafficStatusUpdate,
  val statusHistory: List<TrafficStatusUpdate> = emptyList()
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
      fun update(statusUpdate: TrafficStatusUpdate): Update {
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
      const val TRAFFIC_COLLECTION_NAME = "traffic"
    }
  }

  class Builder {
    private lateinit var message: ConsumerMessage
    private lateinit var time: Instant
    private var status: TrafficStatus = TrafficStatus.PROCESSING

    fun message(message: ConsumerMessage) = apply { this.message = message }
    fun time(time: Instant) = apply { this.time = time }
    fun status(status: TrafficStatus) = apply { this.status = status }

    fun build(): Traffic {
      val statusUpdate = TrafficStatusUpdate(status, time)
      return Traffic(message.traceId, message.topic, message, time, statusUpdate, listOf(statusUpdate))
    }
  }

}