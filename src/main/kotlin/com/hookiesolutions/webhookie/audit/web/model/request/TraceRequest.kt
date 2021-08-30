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

import com.hookiesolutions.webhookie.audit.domain.TraceStatus
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/3/21 14:40
 */
data class TraceRequest(
  val subscriptionId: String? = null,
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
    private var subscriptionId: String? = null
    private var traceId: String? = null
    private var topic: String? = null
    private var applicationId: String? = null
    private var entity: String? = null
    private var callbackId: String? = null
    private var status: List<TraceStatus> = emptyList()
    private var from: Instant? = null
    private var to: Instant? = null

    fun subscriptionId(subscriptionId: String?) = apply { this.subscriptionId = subscriptionId }
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
        subscriptionId = subscriptionId,
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
