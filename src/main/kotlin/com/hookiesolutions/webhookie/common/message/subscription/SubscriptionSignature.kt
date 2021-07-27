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

package com.hookiesolutions.webhookie.common.message.subscription

import org.springframework.http.HttpHeaders
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
data class SubscriptionSignature(
  val keyId: String,
  val algorithm: String,
  val traceId: String,
  val spanId: String,
  val date: String,
  val signature: String
) {
  private val signatureHeader: String
    get() = "keyId=$keyId,algorithm=$algorithm,headers=(request-target) date x-trace-id x-span-id,signature=$signature"

  val headers: Map<String, String>
    get() = mapOf(
      "Date" to date,
      "x-trace-id" to traceId,
      "x-span-id" to spanId,
      HttpHeaders.AUTHORIZATION to "Signature $signatureHeader"
    )

  class Builder {
    private lateinit var keyId: String
    private lateinit var algorithm: String
    private lateinit var traceId: String
    private lateinit var spanId: String
    private lateinit var date: String
    private lateinit var signature: String

    fun keyId(keyId: String) = apply { this.keyId = keyId }
    fun algorithm(algorithm: String) = apply { this.algorithm = algorithm }
    fun traceId(traceId: String) = apply { this.traceId = traceId }
    fun spanId(spanId: String) = apply { this.spanId = spanId }
    fun date(date: String) = apply { this.date = date }
    fun date(instant: Instant) = apply { this.date = instant.toString() }
    fun signature(signature: String) = apply { this.signature = signature }

    fun build() = SubscriptionSignature(keyId, algorithm, traceId, spanId, date, signature)
  }
}
