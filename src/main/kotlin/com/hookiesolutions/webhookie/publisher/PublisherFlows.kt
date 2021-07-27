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
