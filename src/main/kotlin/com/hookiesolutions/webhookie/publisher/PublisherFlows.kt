package com.hookiesolutions.webhookie.publisher

import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.message.publisher.GenericPublisherMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherOtherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import org.slf4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 6/12/20 23:20
 */
@Configuration
class PublisherFlows(
  private val publisher: SubscriptionPublisher,
  private val publisherSuccessChannel: SubscribableChannel,
  private val publisherResponseErrorChannel: SubscribableChannel,
  private val publisherRequestErrorChannel: SubscribableChannel,
  private val publisherOtherErrorChannel: SubscribableChannel,
  private val internalSubscriptionChannel: MessageChannel,
  private val retryablePublisherErrorChannel: MessageChannel
) {
  @Bean
  fun publishSubscriptionFlow(
    globalPublisherErrorChannel: MessageChannel
  ): IntegrationFlow {
    return integrationFlow {
      channel(SUBSCRIPTION_CHANNEL_NAME)
      enrichHeaders {
        this.defaultOverwrite(true)
        this.errorChannel(globalPublisherErrorChannel)
      }
      channel(internalSubscriptionChannel)
    }
  }

  @Bean
  fun publisherErrorHandler(
    globalPublisherErrorChannel: MessageChannel,
    log: Logger
  ): IntegrationFlow {
    return integrationFlow {
      channel(globalPublisherErrorChannel)
      handle {
        log.error("Unexpected error occurred publishing message: '{}', '{}", it.payload, it.headers)
      }
    }
  }

  @Bean
  fun internalSubscriptionFlow(): IntegrationFlow {
    return integrationFlow {
      channel(internalSubscriptionChannel)
      transform<SignableSubscriptionMessage> { publisher.publish(it) }
      split()
      routeToRecipients {
        recipient<GenericPublisherMessage>(publisherSuccessChannel) { it is PublisherSuccessMessage }
        recipient<GenericPublisherMessage>(publisherResponseErrorChannel) { it is PublisherResponseErrorMessage }
        recipient<GenericPublisherMessage>(publisherRequestErrorChannel) { it is PublisherRequestErrorMessage }
        recipient<GenericPublisherMessage>(publisherOtherErrorChannel) { it is PublisherOtherErrorMessage }
        recipient<GenericPublisherMessage>(retryablePublisherErrorChannel) { it is PublisherErrorMessage }
      }
    }
  }
}
