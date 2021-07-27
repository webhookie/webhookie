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
