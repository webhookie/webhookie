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

import com.hookiesolutions.webhookie.common.message.publisher.PublisherOtherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import com.hookiesolutions.webhookie.common.message.subscription.MissingSubscriptionMessage
import org.springframework.http.HttpHeaders
import org.springframework.util.CollectionUtils
import java.time.Instant

data class SpanHttpResponse (
  val time: Instant,
  val statusCode: Int,
  val body: String,
  val headers: HttpHeaders,
  val retryNo: Int
) {
  class Keys {
    companion object {
      const val KEY_STATUS_CODE = "statusCode"
    }
  }
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
      this.retryNo = message.subscriptionMessage.totalNumberOfTries
    }

    fun message(message: PublisherRequestErrorMessage) = apply {
      this.statusCode = -1
      this.body = message.reason
      this.headers = message.headers
      this.retryNo = message.subscriptionMessage.totalNumberOfTries
    }

    fun message(message: PublisherOtherErrorMessage) = apply {
      val headers = CollectionUtils.unmodifiableMultiValueMap(CollectionUtils.toMultiValueMap(mapOf("spanId" to listOf(message.spanId))))
      this.statusCode = -2
      this.body = message.reason
      this.headers = HttpHeaders(headers)
      this.retryNo = message.subscriptionMessage.totalNumberOfTries
    }

    fun message(message: MissingSubscriptionMessage) = apply {
      val headers = CollectionUtils.unmodifiableMultiValueMap(CollectionUtils.toMultiValueMap(mapOf("spanId" to listOf(message.spanId))))
      this.statusCode = -3
      this.body = message.reason
      this.headers = HttpHeaders(headers)
      this.retryNo = -1
    }

    fun message(message: PublisherResponseErrorMessage) = apply {
      this.statusCode = message.response.status.value()
      this.body = message.response.body()
      this.headers = message.response.headers
      this.retryNo = message.subscriptionMessage.totalNumberOfTries
    }

    fun build(): SpanHttpResponse {
      return SpanHttpResponse(time, statusCode, body, headers, retryNo)
    }
  }
}
