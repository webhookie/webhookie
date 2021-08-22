/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

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
import com.hookiesolutions.webhookie.common.message.subscription.MissingSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.RetryableSubscriptionMessage
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
  fun createSpanFlow(): IntegrationFlow {
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
      filter<RetryableSubscriptionMessage> { it.isResend() && it.isFirstRetryInCycle() }
      transform<Message<RetryableSubscriptionMessage>> { spanService.markAsRetrying(it) }
      split()
      transform<Span> { SSENotification.SpanNotification.markedRetrying(it) }
      channel(sseChannel)
    }
  }

  @Bean
  fun addRetryFlow(): IntegrationFlow {
    return integrationFlow {
      channel(DELAYED_SUBSCRIPTION_CHANNEL_NAME)
      filter<RetryableSubscriptionMessage> { it.isResend() && !it.isFirstRetryInCycle() }
      transform<Message<RetryableSubscriptionMessage>> { spanService.addRetry(it.payload) }
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
  fun successResponseFlow(): IntegrationFlow {
    return integrationFlow {
      channel(Constants.Channels.Publisher.PUBLISHER_SUCCESS_CHANNEL)
      transform<PublisherSuccessMessage> { spanService.updateWithSuccessResponse(it) }
      split()
      transform<Span> { SSENotification.SpanNotification.success(it) }
      channel(sseChannel)
    }
  }

  @Bean
  fun requestErrorFlow(): IntegrationFlow {
    return integrationFlow {
      channel(Constants.Channels.Publisher.PUBLISHER_REQUEST_ERROR_CHANNEL)
      transform<PublisherRequestErrorMessage> { spanService.updateWithClientError(it) }
      split()
      transform<Span> { SSENotification.SpanNotification.failedWithClientError(it) }
      channel(sseChannel)
    }
  }

  @Bean
  fun retryableResponseErrorFlow(): IntegrationFlow {
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
  fun nonRetryableResponseErrorFlow(): IntegrationFlow {
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
  fun otherErrorFlow(): IntegrationFlow {
    return integrationFlow {
      channel(Constants.Channels.Publisher.PUBLISHER_OTHER_ERROR_CHANNEL)
      transform<PublisherOtherErrorMessage> { spanService.updateWithOtherError(it) }
      split()
      transform<Span> { SSENotification.SpanNotification.failedWithOtherError(it) }
      channel(sseChannel)
    }
  }

  @Bean
  fun subscriptionMissingFlow() = integrationFlow {
    channel(Constants.Channels.Subscription.MISSING_SUBSCRIPTION_CHANNEL_NAME)
    transform<MissingSubscriptionMessage> { spanService.updateWithSubscriptionError(it) }
    split()
    transform<Span> { SSENotification.SpanNotification.failedWithSubscriptionError(it) }
    channel(sseChannel)
  }

  @Bean
  fun subscriptionErrorFlow(): IntegrationFlow {
    return integrationFlow {
      channel(Constants.Channels.Subscription.SUBSCRIPTION_ERROR_CHANNEL_NAME)
      handle { payload: SubscriptionMessageHandlingException, _: MessageHeaders ->
        log.warn("Error Occurred: '{}' for trace: {}, span: {}", payload.reason, payload.traceId, payload.spanId)
      }
    }
  }
}
