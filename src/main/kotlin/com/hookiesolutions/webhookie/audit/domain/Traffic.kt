package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Traffic.Keys.Companion.KEY_CONSUMER_MESSAGE
import com.hookiesolutions.webhookie.audit.domain.Traffic.Keys.Companion.TRAFFIC_COLLECTION_NAME
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.ConsumerMessage.Keys.Companion.KEY_TRACE_ID
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/3/21 18:17
 */
@Document(collection = TRAFFIC_COLLECTION_NAME)
@TypeAlias("traffic")
@CompoundIndexes(
  CompoundIndex(
    name = "traffic.traceId",
    def = "{'consumerMessage.traceId' : 1}",
    unique = true
  ),
  CompoundIndex(
    name = "traffic.topic",
    def = "{'consumerMessage.topic' : 1}",
    unique = false
  )
)
data class Traffic(
  val consumerMessage: ConsumerMessage,
  val time: Instant,
  val status: TrafficStatus = TrafficStatus.PROCESSING
): AbstractEntity() {

  fun traceId(): String = consumerMessage.traceId

  class Queries {
    companion object {
      fun byTraceId(traceId: String): Criteria {
        return where("$KEY_CONSUMER_MESSAGE.$KEY_TRACE_ID").`is`(traceId)
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_CONSUMER_MESSAGE = "consumerMessage"
      const val TRAFFIC_COLLECTION_NAME = "traffic"
    }
  }

  class Builder {
    private lateinit var message: ConsumerMessage
    private lateinit var time: Instant

    fun message(message: ConsumerMessage) = apply { this.message = message }
    fun time(time: Instant) = apply { this.time = time }

    fun build() = Traffic(message, time)
  }

}
