package com.hookiesolutions.webhookie.publisher

import com.hookiesolutions.webhookie.common.Constants
import com.hookiesolutions.webhookie.common.message.publisher.GenericPublisherMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import org.slf4j.Logger
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 9/12/20 00:42
 */
@Service
class SubscriberClient(
  private val clientFactory: HttpClientFactory,
  private val log: Logger
) {
  fun publish(msg: SubscriptionMessage): Mono<GenericPublisherMessage> {
    val webClient = clientFactory.createClientFor(
      msg.subscription.callbackUrl,
      msg.subscription.httpMethod,
      msg.originalMessage.mediaType
    )

    log.info("Publishing message to '{}'", msg.subscription.callbackUrl)

    return webClient
      .body(BodyInserters.fromValue(msg.originalMessage.payload))
      .header(Constants.Queue.Headers.WH_HEADER_SPAN_ID, msg.spanId)
      .headers { msg.originalMessage.addMessageHeaders(it) }
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