package com.hookiesolutions.webhookie.audit

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.service.SpanService
import com.hookiesolutions.webhookie.audit.web.model.SSENotification
import com.hookiesolutions.webhookie.common.Constants
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.BLOCKED_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.DELAYED_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.exception.messaging.SubscriptionMessageHandlingException
import com.hookiesolutions.webhookie.common.message.publisher.GenericPublisherMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherOtherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import org.slf4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.core.GenericSelector
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.SubscribableChannel

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/6/21 13:55
 */
@Configuration
class SpanFlows(
  private val log: Logger,
  private val spanService: SpanService,
  private val retryableErrorSelector: GenericSelector<GenericPublisherMessage>,
  private val sseChannel: SubscribableChannel
) {
  @Bean
  fun logSubscriptionMessage(): IntegrationFlow {
    return integrationFlow {
      channel(SUBSCRIPTION_CHANNEL_NAME)
      filter<SignableSubscriptionMessage> { it.isNew() }
      transform<SignableSubscriptionMessage> { spanService.createSpan(it) }
      split()
      transform<Span> { SSENotification.SpanNotification.created(it) }
      channel(sseChannel)
    }
  }

  @Bean
  fun markAsRetryingFlow(): IntegrationFlow {
    return integrationFlow {
      channel(DELAYED_SUBSCRIPTION_CHANNEL_NAME)
      filter<SignableSubscriptionMessage> { it.isResend() && it.isFirstRetryInCycle() }
      transform<Message<SignableSubscriptionMessage>> { spanService.markAsRetrying(it) }
      split()
      transform<Span> { SSENotification.SpanNotification.markedRetrying(it) }
      channel(sseChannel)
    }
  }

  @Bean
  fun addRetryFlow(): IntegrationFlow {
    return integrationFlow {
      channel(DELAYED_SUBSCRIPTION_CHANNEL_NAME)
      filter<SignableSubscriptionMessage> { it.isResend() && !it.isFirstRetryInCycle() }
      transform<Message<SignableSubscriptionMessage>> { spanService.addRetry(it.payload) }
      split()
      transform<Span> { SSENotification.SpanNotification.isRetrying(it) }
      channel(sseChannel)
    }
  }

  @Bean
  fun blockSpanFlow(): IntegrationFlow {
    return integrationFlow {
      channel(BLOCKED_SUBSCRIPTION_CHANNEL_NAME)
      transform<BlockedSubscriptionMessageDTO> { spanService.blockSpan(it) }
      split()
      transform<Span> { SSENotification.SpanNotification.blocked(it) }
      channel(sseChannel)
    }
  }

  @Bean
  fun logPublisherSuccessMessage(): IntegrationFlow {
    return integrationFlow {
      channel(Constants.Channels.Publisher.PUBLISHER_SUCCESS_CHANNEL)
      transform<PublisherSuccessMessage> { spanService.updateWithSuccessResponse(it) }
      split()
      transform<Span> { SSENotification.SpanNotification.success(it) }
      channel(sseChannel)
    }
  }

  @Bean
  fun logPublisherRequestErrorMessage(): IntegrationFlow {
    return integrationFlow {
      channel(Constants.Channels.Publisher.PUBLISHER_REQUEST_ERROR_CHANNEL)
      transform<PublisherRequestErrorMessage> { spanService.updateWithClientError(it) }
      split()
      transform<Span> { SSENotification.SpanNotification.failedWithClientError(it) }
      channel(sseChannel)
    }
  }

  @Bean
  fun logPublisherRetryableResponseErrorMessage(): IntegrationFlow {
    return integrationFlow {
      channel(Constants.Channels.Publisher.PUBLISHER_RESPONSE_ERROR_CHANNEL)
      filter<PublisherResponseErrorMessage> { retryableErrorSelector.accept(it) }
      transform<PublisherResponseErrorMessage> { spanService.updateWithRetryableServerError(it) }
      split()
      transform<Span> { SSENotification.SpanNotification.failedWithServerError(it) }
      channel(sseChannel)
    }
  }

  @Bean
  fun logPublisherNonRetryableResponseErrorMessage(): IntegrationFlow {
    return integrationFlow {
      channel(Constants.Channels.Publisher.PUBLISHER_RESPONSE_ERROR_CHANNEL)
      filter<PublisherResponseErrorMessage> { !retryableErrorSelector.accept(it) }
      transform<PublisherResponseErrorMessage> { spanService.updateWithNonRetryableServerError(it) }
      split()
      transform<Span> { SSENotification.SpanNotification.failedWithStatusUpdate(it) }
      channel(sseChannel)
    }
  }

  @Bean
  fun logPublisherOtherErrorMessage(): IntegrationFlow {
    return integrationFlow {
      channel(Constants.Channels.Publisher.PUBLISHER_OTHER_ERROR_CHANNEL)
      transform<PublisherOtherErrorMessage> { spanService.updateWithOtherError(it) }
      split()
      transform<Span> { SSENotification.SpanNotification.failedWithOtherError(it) }
      channel(sseChannel)
    }
  }

  @Bean
  fun logSubscriptionErrorMessage(): IntegrationFlow {
    return integrationFlow {
      channel(Constants.Channels.Subscription.SUBSCRIPTION_ERROR_CHANNEL_NAME)
      handle { payload: SubscriptionMessageHandlingException, _: MessageHeaders ->
        log.warn("Error Occurred: '{}' for trace: {}, span: {}", payload.reason, payload.traceId, payload.spanId)
      }
    }
  }
}
