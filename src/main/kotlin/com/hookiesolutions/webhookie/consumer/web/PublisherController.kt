package com.hookiesolutions.webhookie.consumer.web

import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.HEADER_CONTENT_TYPE
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_AUTHORIZED_SUBSCRIBER
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TOPIC
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_TRACE_ID
import org.slf4j.Logger
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.messaging.SubscribableChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/12/20 13:47
 */
@RestController
class PublisherController(
  private val log: Logger,
  private val internalConsumerChannel: SubscribableChannel
) {
  @PostMapping("/publish", produces = [MediaType.TEXT_PLAIN_VALUE])
  fun publishEvent(
    @RequestBody body: ByteArray,
    @RequestHeader(WH_HEADER_TOPIC, required = true) topic: String,
    @RequestHeader(WH_HEADER_TRACE_ID, required = true) traceId: String,
    @RequestHeader(HttpHeaders.CONTENT_TYPE, required = true) contentType: String,
    @RequestHeader(WH_HEADER_AUTHORIZED_SUBSCRIBER, required = false, defaultValue = "") authorizedSubscribers: List<String>
  ): Mono<String> {
    log.info("Publishing a message to event queue....")

    val messageBuilder = MessageBuilder
      .withPayload(body)
      .setHeader(WH_HEADER_TOPIC, topic)
      .setHeader(WH_HEADER_TRACE_ID, traceId)
      .setHeader(HEADER_CONTENT_TYPE, contentType)
    if(authorizedSubscribers.isNotEmpty()) {
      messageBuilder.setHeader(WH_HEADER_AUTHORIZED_SUBSCRIBER, authorizedSubscribers)
    }
    internalConsumerChannel
      .send(messageBuilder.build())
    return "OK".toMono()
  }
}