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

