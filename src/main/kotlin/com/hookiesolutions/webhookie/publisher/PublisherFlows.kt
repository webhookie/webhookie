package com.hookiesolutions.webhookie.publisher

import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.message.publisher.GenericPublisherMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherOtherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import com.hookiesolutions.webhookie.publisher.config.PublisherProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
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
  private val subscriberClient: SubscriberClient,
  private val properties: PublisherProperties,
  private val publisherSuccessChannel: SubscribableChannel,
  private val publisherResponseErrorChannel: SubscribableChannel,
  private val publisherRequestErrorChannel: SubscribableChannel,
  private val publisherOtherErrorChannel: SubscribableChannel,
  private val retryResponseErrorChannel: MessageChannel
) {
  @Bean
  fun publishSubscriptionMessage(clientFactory: HttpClientFactory): IntegrationFlow {
    return integrationFlow {
      channel(SUBSCRIPTION_CHANNEL_NAME)
      transform<SubscriptionMessage> { subscriberClient.publish(it) }
      split()
      routeToRecipients {
        this.recipient<GenericPublisherMessage>(publisherSuccessChannel) { p -> p is PublisherSuccessMessage }
        this.recipient<GenericPublisherMessage>(publisherResponseErrorChannel) { p -> p is PublisherResponseErrorMessage }
        this.recipient<GenericPublisherMessage>(publisherRequestErrorChannel) { p -> p is PublisherRequestErrorMessage }
        this.recipient<GenericPublisherMessage>(publisherOtherErrorChannel) { p -> p is PublisherOtherErrorMessage }
        this.recipient<GenericPublisherMessage>(retryResponseErrorChannel) {
          if(it.subscriptionMessage.numberOfRetries >= properties.maxRetry) {
            return@recipient false
          }

          (it is PublisherRequestErrorMessage) || (
              it is PublisherResponseErrorMessage && (
                  it.status.is5xxServerError || (it.status == HttpStatus.NOT_FOUND)
                  )
              )
        }
        this.defaultOutputChannel(publisherSuccessChannel)
      }
    }
  }

  @Bean
  fun handleRetryFlow(): IntegrationFlow {
    return integrationFlow {
      channel(retryResponseErrorChannel)
      transform<GenericPublisherMessage> {
        it.subscriptionMessage.copy(
          delay = subscriberClient.calculateDelayForSubscription(it.subscriptionMessage),
          numberOfRetries = it.subscriptionMessage.numberOfRetries + 1
        )
      }
      channel(SUBSCRIPTION_CHANNEL_NAME)
    }
  }
}
