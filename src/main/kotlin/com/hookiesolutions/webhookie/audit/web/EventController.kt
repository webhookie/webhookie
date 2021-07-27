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

package com.hookiesolutions.webhookie.audit.web

import com.hookiesolutions.webhookie.audit.web.TrafficAPIDocs.Companion.REQUEST_MAPPING_TRAFFIC
import com.hookiesolutions.webhookie.audit.web.TrafficController.Companion.REQUEST_MAPPING_TRAFFIC_SPAN
import com.hookiesolutions.webhookie.audit.web.model.SSENotification
import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.SubscribableChannel
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.time.Duration
import java.util.*

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 16/6/21 00:22
 */
@RestController
@SecurityRequirement(name = OAUTH2_SCHEME)
@RequestMapping(REQUEST_MAPPING_TRAFFIC)
class EventController(
  private val sseChannel: SubscribableChannel,
  private val log: Logger
) {
  @PostMapping(
    "${REQUEST_MAPPING_TRAFFIC_SPAN}/events",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
  )
  fun eventsForSpans(@RequestBody spanIds: List<String>): Flux<ServerSentEvent<Any>> {
    return Flux.create { sink ->
      val handler = createHandlerUsing(sink, spanIds)
      val heartbeatDisposable = createHeartbeatHandlerUsing(sink)
        .subscribe()

      sink.onCancel {
        heartbeatDisposable.dispose()
        sseChannel.unsubscribe(handler)
      }
      sseChannel.subscribe(handler)
    }
  }

  private fun createHeartbeatHandlerUsing(sink: FluxSink<ServerSentEvent<Any>>): Flux<Long> {
    return Flux.interval(Duration.ofSeconds(40L))
      .doOnNext {
        val payload = SSENotification(UUID.randomUUID().toString(), "Heartbeat", mapOf("UP" to it))
        val sse = ServerSentEvent
          .builder<Any>()
          .event(payload.event)
          .data(payload.data)
          .comment("Heartbeat message")
          .id(payload.id)
          .build()
        sink.next(sse)
      }
      .doOnNext { log.debug("Heartbeat SSE sent... ") }
  }

  private fun createHandlerUsing(sink: FluxSink<ServerSentEvent<Any>>, spanIds: List<String>): MessageHandler {
    return MessageHandler { msg ->
      val notification = msg.payload as SSENotification
      if(spanIds.contains(notification.id)) {
        if(log.isDebugEnabled) {
          log.debug("Emitting event '{}'", notification)
        }
        val sse = ServerSentEvent.builder<Any>()
          .event(notification.event)
          .data(notification.data)
          .comment(msg.payload.javaClass.name)
          .id(UUID.randomUUID().toString())
          .build()
        sink.next(sse)
      }
    }
  }
}

