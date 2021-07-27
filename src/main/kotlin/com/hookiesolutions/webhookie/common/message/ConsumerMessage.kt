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

package com.hookiesolutions.webhookie.common.message

import org.springframework.data.annotation.Transient
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 12:31
 */
data class ConsumerMessage(
  override val traceId: String,
  val topic: String,
  val contentType: String,
  val authorizedSubscribers: Set<String>,
  val payload: ByteArray,
  val headers: Map<String, Any>,
): WebhookieMessage {
  fun addMessageHeaders(headers: HttpHeaders) {
    this.headers
      .forEach {
        val stringValue = it.value as? String
        if (stringValue != null) {
          headers.addIfAbsent(it.key, stringValue)
        } else {
          @Suppress("UNCHECKED_CAST") val listValue = it.value as? List<String>
          if(listValue != null) {
            headers.addAll(it.key, listValue)
          }
        }
      }
  }

  @Transient
  fun  mediaType(): MediaType = MediaType.parseMediaType(contentType)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ConsumerMessage) return false

    if (traceId != other.traceId) return false
    if (topic != other.topic) return false
    if (contentType != other.contentType) return false
    if (authorizedSubscribers != other.authorizedSubscribers) return false
    if (!payload.contentEquals(other.payload)) return false
    if (headers != other.headers) return false

    return true
  }

  override fun hashCode(): Int {
    var result = traceId.hashCode()
    result = 31 * result + topic.hashCode()
    result = 31 * result + contentType.hashCode()
    result = 31 * result + authorizedSubscribers.hashCode()
    result = 31 * result + payload.contentHashCode()
    result = 31 * result + headers.hashCode()
    return result
  }
}
