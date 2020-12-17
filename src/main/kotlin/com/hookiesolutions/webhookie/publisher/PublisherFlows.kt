package com.hookiesolutions.webhookie.publisher

import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.UNSUCCESSFUL_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.message.publisher.GenericPublisherMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherOtherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
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
import java.time.Duration

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
  private val internalSubscriptionChannel: MessageChannel,
  private val requiresRetrySelector: GenericSelector<GenericPublisherMessage>,
  private val requiresBlockSelector: GenericSelector<GenericPublisherMessage>,
  private val delayCalculator: GenericTransformer<SubscriptionMessage, Duration>
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
        recipient<GenericPublisherMessage>(publisherSuccessChannel) { it is PublisherSuccessMessage }
        recipient<GenericPublisherMessage>(publisherResponseErrorChannel) { it is PublisherResponseErrorMessage }
        recipient<GenericPublisherMessage>(publisherRequestErrorChannel) { it is PublisherRequestErrorMessage }
        recipient<GenericPublisherMessage>(publisherOtherErrorChannel) { it is PublisherOtherErrorMessage }
        recipient<GenericPublisherMessage>(retrySubscriptionMessageChannel) { requiresRetrySelector.accept(it) }
        recipient<GenericPublisherMessage>(UNSUCCESSFUL_SUBSCRIPTION_CHANNEL_NAME) { requiresBlockSelector.accept(it) }
      }
    }
  }

  @Bean
  fun retrySubscriptionMessageFlow(): IntegrationFlow {
    return integrationFlow {
      channel(retrySubscriptionMessageChannel)
      transform<PublisherErrorMessage> {
        it.subscriptionMessage.copy(
          delay = delayCalculator.transform(it.subscriptionMessage),
          numberOfRetries = it.subscriptionMessage.numberOfRetries + 1
        )
      }
      channel(SUBSCRIPTION_CHANNEL_NAME)
    }
  }
}
