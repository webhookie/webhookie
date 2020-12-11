package com.hookiesolutions.webhookie.publisher.config

import com.hookiesolutions.webhookie.common.message.publisher.GenericPublisherMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.integration.core.GenericSelector
import org.springframework.integration.transformer.GenericTransformer
import java.time.Duration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 11/12/20 15:31
 */
@Configuration
class PublisherConfig(
  private val properties: PublisherProperties
) {
  @Bean
  fun toRetryableSubscriptionMessage(delayCalculator: GenericTransformer<SubscriptionMessage, Duration>): GenericTransformer<GenericPublisherMessage, SubscriptionMessage> {
    return GenericTransformer {
      it.subscriptionMessage.copy(
        delay = delayCalculator.transform(it.subscriptionMessage),
        numberOfRetries = it.subscriptionMessage.numberOfRetries + 1
      )
    }
  }

  @Bean
  fun delayCalculator(): GenericTransformer<SubscriptionMessage, Duration> {
    return GenericTransformer {
      Duration.ofSeconds(it.delay.seconds * properties.retry.multiplier + properties.retry.initialInterval)
    }
  }

  @Bean
  fun requiresRetrySelector(): GenericSelector<GenericPublisherMessage> {
    return GenericSelector {
      val message = it
      if(message.subscriptionMessage.numberOfRetries >= properties.retry.maxRetry) {
        return@GenericSelector false
      }

      (message is PublisherRequestErrorMessage) || (
          message is PublisherResponseErrorMessage && (
              message.status.is5xxServerError || (message.status == HttpStatus.NOT_FOUND)
              )
          )
    }
  }
}