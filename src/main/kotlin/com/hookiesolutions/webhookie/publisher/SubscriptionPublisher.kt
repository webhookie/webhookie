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
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

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

    return Mono
      .delay(msg.delay)
      .flatMap {
        WebClient
          .create(msg.subscription.callback.url)
          .method(msg.subscription.callback.httpMethod)
          .contentType(msg.originalMessage.mediaType)
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
