package com.hookiesolutions.webhookie.publisher

import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_SPAN_ID
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.GenericPublisherMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherOtherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import org.slf4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.messaging.SubscribableChannel
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 6/12/20 23:20
 */
@Configuration
class PublisherFlows(
  private val log: Logger,
  private val publisherSuccessChannel: SubscribableChannel,
  private val publisherResponseErrorChannel: SubscribableChannel,
  private val publisherRequestErrorChannel: SubscribableChannel,
  private val publisherOtherErrorChannel: SubscribableChannel,
) {
  @Bean
  fun publishSubscriptionMessage(clientFactory: HttpClientFactory): IntegrationFlow {
    return integrationFlow {
      channel(SUBSCRIPTION_CHANNEL_NAME)
      transform<SubscriptionMessage> { msg ->

        val webClient = clientFactory.createClientFor(
          msg.subscription.callbackUrl,
          msg.subscription.httpMethod,
          msg.originalMessage.mediaType
        )

        webClient
          .body(BodyInserters.fromValue(msg.originalMessage.payload))
          .header(WH_HEADER_SPAN_ID, msg.spanId)
          .headers { msg.originalMessage.addMessageHeaders(it) }
          .retrieve()
          .toEntity(ByteArray::class.java)
          .map { GenericPublisherMessage.success(msg, it) }
          .onErrorResume(WebClientRequestException::class.java) {
            log.error("was unable to :'{}'", it.localizedMessage)
            GenericPublisherMessage.requestError(msg, it).toMono()
          }
          .onErrorResume(WebClientResponseException::class.java) {
            log.error("was unable to :'{}'", it.localizedMessage)
            GenericPublisherMessage.responseError(msg, it).toMono()
          }
          .onErrorResume {
            log.error("was unable to :'{}'", it.localizedMessage)
            GenericPublisherMessage.unknownError(msg, it).toMono()
          }
      }
      split()
      routeToRecipients {
        this.recipient<Any>(publisherSuccessChannel) { p -> p is PublisherSuccessMessage }
        this.recipient<Any>(publisherResponseErrorChannel) { p -> p is PublisherResponseErrorMessage }
        this.recipient<Any>(publisherRequestErrorChannel) { p -> p is PublisherRequestErrorMessage }
        this.recipient<Any>(publisherOtherErrorChannel) { p -> p is PublisherOtherErrorMessage }
        this.defaultOutputChannel(publisherSuccessChannel)
      }
    }
  }
}
