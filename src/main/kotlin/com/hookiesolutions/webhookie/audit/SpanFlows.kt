package com.hookiesolutions.webhookie.audit

import com.hookiesolutions.webhookie.audit.service.SpanService
import com.hookiesolutions.webhookie.common.Constants
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.BLOCKED_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.DELAYED_SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.SUBSCRIPTION_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.exception.messaging.SubscriptionMessageHandlingException
import com.hookiesolutions.webhookie.common.message.publisher.PublisherOtherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import org.slf4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.integrationFlow
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/6/21 13:55
 */
@Configuration
class SpanFlows(
  private val log: Logger,
  private val spanService: SpanService
) {
  @Bean
  fun logSubscriptionMessage(): IntegrationFlow {
    return integrationFlow {
      channel(SUBSCRIPTION_CHANNEL_NAME)
      filter<SignableSubscriptionMessage> { it.isNew() }
      handle { payload: SignableSubscriptionMessage, _: MessageHeaders ->
        spanService.createSpan(payload)
      }
    }
  }

  @Bean
  fun retryingSpanFlow(): IntegrationFlow {
    return integrationFlow {
      channel(DELAYED_SUBSCRIPTION_CHANNEL_NAME)
      filter<SignableSubscriptionMessage> { it.isResend() }
      handle { payload: Message<SignableSubscriptionMessage>, _: MessageHeaders ->
        spanService.retrying(payload)
      }
    }
  }

  @Bean
  fun blockSpanFlow(): IntegrationFlow {
    return integrationFlow {
      channel(BLOCKED_SUBSCRIPTION_CHANNEL_NAME)
      handle { payload: BlockedSubscriptionMessageDTO, _: MessageHeaders ->
        spanService.blockSpan(payload)
      }
    }
  }

  @Bean
  fun logPublisherSuccessMessage(): IntegrationFlow {
    return integrationFlow {
      channel(Constants.Channels.Publisher.PUBLISHER_SUCCESS_CHANNEL)
      handle { payload: PublisherSuccessMessage, _: MessageHeaders ->
        spanService.updateWithSuccessResponse(payload)
      }
    }
  }

  @Bean
  fun logPublisherRequestErrorMessage(): IntegrationFlow {
    return integrationFlow {
      channel(Constants.Channels.Publisher.PUBLISHER_REQUEST_ERROR_CHANNEL)
      handle { payload: PublisherRequestErrorMessage, _: MessageHeaders ->
        spanService.updateWithClientError(payload)
      }
    }
  }

  @Bean
  fun logPublisherResponseErrorMessage(): IntegrationFlow {
    return integrationFlow {
      channel(Constants.Channels.Publisher.PUBLISHER_RESPONSE_ERROR_CHANNEL)
      handle { payload: PublisherResponseErrorMessage, _: MessageHeaders ->
        spanService.updateWithServerError(payload)
      }
    }
  }

  @Bean
  fun logPublisherOtherErrorMessage(): IntegrationFlow {
    return integrationFlow {
      channel(Constants.Channels.Publisher.PUBLISHER_OTHER_ERROR_CHANNEL)
      handle { payload: PublisherOtherErrorMessage, _: MessageHeaders ->
        spanService.updateWithOtherError(payload)
      }
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
