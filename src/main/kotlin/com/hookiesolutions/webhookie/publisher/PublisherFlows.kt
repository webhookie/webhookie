package com.hookiesolutions.webhookie.publisher

import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.UNSUCCESSFUL_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.message.publisher.GenericPublisherMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherOtherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.UnsuccessfulSubscriptionMessage
import com.hookiesolutions.webhookie.publisher.config.PublisherProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.core.GenericSelector
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.transformer.GenericTransformer
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 6/12/20 23:20
 */
@Configuration
@EnableConfigurationProperties(PublisherProperties::class)
class PublisherFlows(
  private val publisher: SubscriptionPublisher,
  private val publisherSuccessChannel: SubscribableChannel,
  private val publisherResponseErrorChannel: SubscribableChannel,
  private val publisherRequestErrorChannel: SubscribableChannel,
  private val publisherOtherErrorChannel: SubscribableChannel,
  private val retrySubscriptionMessageChannel: MessageChannel,
  private val unsuccessfulMessageChannel: MessageChannel,
  private val internalSubscriptionChannel: MessageChannel,
  private val requiresRetrySelector: GenericSelector<GenericPublisherMessage>,
  private val requiresBlockSelector: GenericSelector<GenericPublisherMessage>,
  private val toRetryableSubscriptionMessage: GenericTransformer<GenericPublisherMessage, SubscriptionMessage>,
  private val unsuccessfulMessageTransformer: GenericTransformer<GenericPublisherMessage, UnsuccessfulSubscriptionMessage>
) {
  @Bean
  fun publishSubscriptionFlow(): IntegrationFlow {
    return integrationFlow {
      channel(SUBSCRIPTION_CHANNEL_NAME)
      channel(internalSubscriptionChannel)
    }
  }

  @Bean
  fun internalSubscriptionFlow(): IntegrationFlow {
    return integrationFlow {
      channel(internalSubscriptionChannel)
      transform<SubscriptionMessage> { publisher.publish(it) }
      split()
      routeToRecipients {
        recipient<GenericPublisherMessage>(publisherSuccessChannel) { p -> p is PublisherSuccessMessage }
        recipient<GenericPublisherMessage>(publisherResponseErrorChannel) { p -> p is PublisherResponseErrorMessage }
        recipient<GenericPublisherMessage>(publisherRequestErrorChannel) { p -> p is PublisherRequestErrorMessage }
        recipient<GenericPublisherMessage>(publisherOtherErrorChannel) { p -> p is PublisherOtherErrorMessage }
        delegate.recipient(retrySubscriptionMessageChannel, requiresRetrySelector)
        delegate.recipient(unsuccessfulMessageChannel, requiresBlockSelector)
      }
    }
  }

  @Bean
  fun retrySubscriptionMessageFlow(): IntegrationFlow {
    return integrationFlow {
      channel(retrySubscriptionMessageChannel)
      transform(toRetryableSubscriptionMessage)
      channel(SUBSCRIPTION_CHANNEL_NAME)
    }
  }

  @Bean
  fun unsuccessfulMessageFlow(): IntegrationFlow {
    return integrationFlow {
      channel(unsuccessfulMessageChannel)
      transform(unsuccessfulMessageTransformer)
      channel(UNSUCCESSFUL_SUBSCRIPTION_CHANNEL_NAME)
    }
  }
}
