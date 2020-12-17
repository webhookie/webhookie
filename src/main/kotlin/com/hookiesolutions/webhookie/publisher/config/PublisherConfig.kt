package com.hookiesolutions.webhookie.publisher.config

import com.hookiesolutions.webhookie.common.message.publisher.GenericPublisherMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
  fun delayCalculator(): GenericTransformer<SubscriptionMessage, Duration> {
    return GenericTransformer {
      Duration.ofSeconds(it.delay.seconds * properties.retry.multiplier + properties.retry.initialInterval)
    }
  }

  @Bean
  fun retryableErrorSelector(): GenericSelector<GenericPublisherMessage> {
    return GenericSelector { it is PublisherErrorMessage && it.isRetryable }
  }

  @Bean
  fun subscriptionHasReachedMaximumRetrySelector(): GenericSelector<GenericPublisherMessage> {
    return GenericSelector {
      it.subscriptionMessage.numberOfRetries >= properties.retry.maxRetry
    }
  }

  @Bean
  fun requiresRetrySelector(
    subscriptionHasReachedMaximumRetrySelector: GenericSelector<GenericPublisherMessage>,
    retryableErrorSelector: GenericSelector<GenericPublisherMessage>
  ): GenericSelector<GenericPublisherMessage> {
    return GenericSelector {
      retryableErrorSelector.accept(it) && !subscriptionHasReachedMaximumRetrySelector.accept(it)
    }
  }

  @Bean
  fun requiresBlockSelector(
    subscriptionHasReachedMaximumRetrySelector: GenericSelector<GenericPublisherMessage>,
    retryableErrorSelector: GenericSelector<GenericPublisherMessage>
  ): GenericSelector<GenericPublisherMessage> {
    return GenericSelector {
      retryableErrorSelector.accept(it) && subscriptionHasReachedMaximumRetrySelector.accept(it)
    }
  }
}