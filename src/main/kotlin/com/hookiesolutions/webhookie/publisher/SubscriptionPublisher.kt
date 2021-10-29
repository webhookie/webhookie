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

package com.hookiesolutions.webhookie.publisher

import com.hookiesolutions.webhookie.common.Constants
import com.hookiesolutions.webhookie.common.message.publisher.GenericPublisherMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import org.slf4j.Logger
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriUtils
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.nio.charset.StandardCharsets

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 9/12/20 00:42
 */
@Service
class SubscriptionPublisher(
  private val log: Logger
) {
  fun publish(msg: SignableSubscriptionMessage): Mono<GenericPublisherMessage> {
    log.info( "'{}'ing '{}' message to '{}' ({}-{})",
      msg.subscription.callback.httpMethod.name,
      msg.originalMessage.contentType,
      msg.subscription.callback.url,
      msg.traceId,
      msg.spanId
    )

    val decodedUrl = UriUtils.decode(msg.subscription.callback.url, StandardCharsets.UTF_8)

    return Mono
      .defer {
        WebClient
          .create(decodedUrl)
          .method(msg.subscription.callback.httpMethod)
          .contentType(msg.originalMessage.mediaType())
          .body(BodyInserters.fromValue(msg.originalMessage.payload))
          .header(Constants.Queue.Headers.WH_HEADER_SPAN_ID, msg.spanId)
          .headers { msg.addMessageHeaders(it) }
          .retrieve()
          .toEntity(ByteArray::class.java)
          .map { GenericPublisherMessage.success(msg, it) }
          .onErrorResume(WebClientRequestException::class.java) {
            GenericPublisherMessage.requestError(msg, it).toMono()
          }
          .onErrorResume(WebClientResponseException::class.java) {
            GenericPublisherMessage.responseError(msg, it).toMono()
          }
          .onErrorResume {
            GenericPublisherMessage.unknownError(msg, it).toMono()
          }
      }
  }
}
