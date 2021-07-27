/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_STATUS_HISTORY
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_STATUS_UPDATE
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_SUMMARY
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_TIME
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_TOPIC
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.KEY_TRACE_ID
import com.hookiesolutions.webhookie.audit.domain.Trace.Keys.Companion.TRACE_COLLECTION_NAME
import com.hookiesolutions.webhookie.audit.domain.TraceStatusUpdate.Keys.Companion.KEY_STATUS
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.regexField
import com.hookiesolutions.webhookie.common.repository.GenericRepository.Companion.fieldName
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
  val statusHistory: List<TraceStatusUpdate> = emptyList(),
  val summary: TraceSummary = TraceSummary.unknown()
): AbstractEntity() {

  class Queries {
    companion object {
      fun byTraceId(traceId: String): Criteria {
        return where(KEY_TRACE_ID).`is`(traceId)
      }

      fun traceIdRegex(traceId: String): Criteria {
        return regexField(KEY_TRACE_ID, traceId)
      }

      fun statusIn(statusList: List<TraceStatus>): Criteria {
        return where(fieldName(KEY_STATUS_UPDATE, KEY_STATUS)).`in`(statusList.map { it.name })
      }

      fun traceStatusIsNot(status: TraceStatus): Criteria {
        return where(fieldName(KEY_STATUS_UPDATE, KEY_STATUS)).ne(status)
      }

      fun traceTopicIn(topics: List<String>): Criteria {
        return where(KEY_TOPIC).`in`(topics)
      }

      fun traceTopicIs(topics: String): Criteria {
        return where(KEY_TOPIC).`is`(topics)
      }

      fun traceUpdatedAfter(from: Instant): Criteria {
        return where(KEY_TIME).gte(from)
      }

      fun traceUpdatedBefore(from: Instant): Criteria {
        return where(KEY_TIME).lte(from)
      }
    }
  }

  class Updates {
    companion object {
      fun traceStatusUpdate(statusUpdate: TraceStatusUpdate): Update {
        return Update()
          .set(KEY_STATUS_UPDATE, statusUpdate)
          .set(KEY_TIME, statusUpdate.time)
          .addToSet(KEY_STATUS_HISTORY, statusUpdate)
      }

      fun updateSummary(summary: TraceSummary, statusUpdate: TraceStatusUpdate): Update {
        val update = traceStatusUpdate(statusUpdate)
        return update
          .set(KEY_SUMMARY, summary)
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_TRACE_ID = "traceId"
      const val KEY_STATUS_UPDATE = "statusUpdate"
      const val KEY_TOPIC = "topic"
      const val KEY_STATUS_HISTORY = "statusHistory"
      const val KEY_TIME = "time"
      const val KEY_SUMMARY = "summary"
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
