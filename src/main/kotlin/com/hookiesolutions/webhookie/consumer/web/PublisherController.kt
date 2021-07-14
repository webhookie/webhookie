package com.hookiesolutions.webhookie.consumer.web

import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.HEADER_CONTENT_TYPE
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_AUTHORIZED_SUBSCRIBER
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TOPIC
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID_MISSING
import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import com.hookiesolutions.webhookie.consumer.service.TrafficServiceDelegate
import com.hookiesolutions.webhookie.consumer.web.ConsumerAPIDocs.Companion.REQUEST_MAPPING_CONSUMER
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.Logger
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.messaging.Message
import org.springframework.messaging.SubscribableChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/12/20 13:47
 */
@RestController
@SecurityRequirement(name = OAUTH2_SCHEME)
@RequestMapping(REQUEST_MAPPING_CONSUMER)
class PublisherController(
  private val log: Logger,
  private val traceServiceDelegate: TrafficServiceDelegate,
  private val internalConsumerChannel: SubscribableChannel,
) {
  @PostMapping(REQUEST_MAPPING_CONSUMER_EVENT, produces = [MediaType.TEXT_PLAIN_VALUE])
  fun publishEvent(
    @RequestBody body: ByteArray,
    @RequestHeader(WH_HEADER_TOPIC, required = true) topic: String,
    @RequestHeader(WH_HEADER_TRACE_ID, required = false, defaultValue = "") traceId: String,
    @RequestHeader(HttpHeaders.CONTENT_TYPE, required = true) contentType: String,
    @RequestHeader(
      WH_HEADER_AUTHORIZED_SUBSCRIBER,
      required = false,
      defaultValue = ""
    ) authorizedSubscribers: List<String>
  ): Mono<String> {
    return traceServiceDelegate.checkOrGenerateTrace(traceId)
      .doOnError { log.warn("Message was rejected due to duplicate traceId '{}'", traceId) }
      .map {
        log.info("Publishing a message to event queue....")

        val messageBuilder = MessageBuilder
          .withPayload(body)
          .setHeader(WH_HEADER_TOPIC, topic)
          .setHeader(WH_HEADER_TRACE_ID, it)
          .setHeader(HEADER_CONTENT_TYPE, contentType)
        if (authorizedSubscribers.isNotEmpty()) {
          messageBuilder.setHeader(WH_HEADER_AUTHORIZED_SUBSCRIBER, authorizedSubscribers)
        }
        val message = messageBuilder.build()

        internalConsumerChannel.send(message)
      }
      .doOnNext { log.debug("Message with traceId: '{}' is being processed") }
      .map { "OK" }
  }

  @PostMapping("/event2", produces = [MediaType.TEXT_PLAIN_VALUE])
  fun publishEvent2(
    @RequestBody body: Message<ByteArray>,
    @RequestHeader(WH_HEADER_TRACE_ID, required = false, defaultValue = "") traceId: String,
  ): Mono<String> {
    val responseMono = if (body.headers[WH_HEADER_TRACE_ID_MISSING] == "true") {
      sendMessage(body)
    } else {
      traceServiceDelegate.checkOrGenerateTrace(traceId)
        .doOnError { log.warn("Message was rejected due to duplicate traceId '{}'", traceId) }
        .flatMap { sendMessage(body) }
    }

    return responseMono
      .doOnNext { log.debug("Message with traceId: '{}' is being processed") }
      .map { "OK" }
  }

  private fun sendMessage(body: Message<ByteArray>): Mono<Boolean> {
    log.info("Publishing a message to event queue....")
    val msg = MessageBuilder
      .withPayload(body.payload)
      .copyHeaders(body.headers)
      .build()
    return internalConsumerChannel.send(msg)
      .toMono()
  }

  companion object {
    const val REQUEST_MAPPING_CONSUMER_EVENT = "/event"
  }
}
