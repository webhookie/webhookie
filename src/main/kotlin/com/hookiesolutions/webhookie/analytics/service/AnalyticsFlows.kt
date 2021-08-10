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

package com.hookiesolutions.webhookie.analytics.service

import com.hookiesolutions.webhookie.analytics.config.AnalyticsProperties
import com.hookiesolutions.webhookie.analytics.service.model.AnalyticsData
import com.hookiesolutions.webhookie.analytics.service.model.AnalyticsItem
import com.hookiesolutions.webhookie.analytics.service.model.AnalyticsTimeCriteria
import com.hookiesolutions.webhookie.analytics.service.model.SpanAnalyticsItem
import com.hookiesolutions.webhookie.analytics.service.model.SubscriptionAnalyticsItem
import com.hookiesolutions.webhookie.analytics.service.model.TraceAnalyticsItem
import org.slf4j.Logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.aggregator.CorrelationStrategy
import org.springframework.integration.aggregator.HeaderAttributeCorrelationStrategy
import org.springframework.integration.aggregator.MessageGroupProcessor
import org.springframework.integration.aggregator.ReleaseStrategy
import org.springframework.integration.context.IntegrationContextUtils
import org.springframework.integration.core.MessageSource
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec
import org.springframework.integration.dsl.integrationFlow
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/8/21 23:14
 */
@Configuration
@ConditionalOnProperty(prefix = AnalyticsProperties.PROPS_ANALYTICS_PREFIX, value = ["send"], havingValue = "true", matchIfMissing = true)
class AnalyticsFlows(
  private val log: Logger,
  private val analyticsService: AnalyticsService,
  private val trafficService: AnalyticsTrafficServiceDelegate,
  private val subscriptionService: AnalyticsSubscriptionServiceDelegate,
  private val captureAnalyticsAggregateChannel: MessageChannel,
  private val captureAnalyticsTracesChannel: MessageChannel,
  private val captureAnalyticsSpansChannel: MessageChannel,
  private val captureAnalyticsSubscriptionsChannel: MessageChannel,
  private val postAnalyticsDataChannel: MessageChannel,
) {
  @Bean
  fun triggerAnalyticsFlow(
    analyticsMessageSource: MessageSource<AnalyticsTimeCriteria>,
    analyticsPollingSpec: SourcePollingChannelAdapterSpec.() -> Unit
  ) = integrationFlow(analyticsMessageSource, analyticsPollingSpec) {
    routeToRecipients {
      recipient(captureAnalyticsTracesChannel)
      recipient(captureAnalyticsSpansChannel)
      recipient(captureAnalyticsSubscriptionsChannel)
    }
  }

  @Bean
  fun captureAnalyticsTracesFlow() = integrationFlow {
    channel(captureAnalyticsTracesChannel)
    transform<AnalyticsTimeCriteria> { dateCriteria ->
      trafficService.fetchTraceAnalyticsData(dateCriteria)
        .map { TraceAnalyticsItem(it) }
    }
    split()
    channel(captureAnalyticsAggregateChannel)
  }

  @Bean
  fun captureAnalyticsSpansFlow() = integrationFlow {
    channel(captureAnalyticsSpansChannel)
    transform<AnalyticsTimeCriteria> { dateCriteria ->
      trafficService.fetchSpanAnalyticsData(dateCriteria)
        .map { SpanAnalyticsItem(it) }
    }
    split()
    channel(captureAnalyticsAggregateChannel)
  }

  @Bean
  fun captureAnalyticsSubscriptionsFlow() = integrationFlow {
    channel(captureAnalyticsSubscriptionsChannel)
    transform<AnalyticsTimeCriteria> { dateCriteria ->
      subscriptionService.fetchSubscriptionAnalyticsData(dateCriteria)
        .map { SubscriptionAnalyticsItem(it) }
    }
    split()
    channel(captureAnalyticsAggregateChannel)
  }


  @Bean
  fun captureAnalyticsCorrelationStrategy(): CorrelationStrategy {
    return HeaderAttributeCorrelationStrategy(WH_ANALYTICS_SEQ_ID)
  }

  @Bean
  fun captureAnalyticsReleaseStrategy(): ReleaseStrategy {
    return ReleaseStrategy {
      val size = it.one.headers[WH_ANALYTICS_SEQ_SIZE].toString().toInt()
      it.messages.size == size
    }
  }

  @Bean
  fun captureAnalyticsOutputProcessor(): MessageGroupProcessor {
    return MessageGroupProcessor { group ->
      val items = group.messages
        .map { it.payload as AnalyticsItem}

      val one = group.one
      val from = one.headers[WH_ANALYTICS_QUERY_FROM] as Instant
      val to = one.headers[WH_ANALYTICS_QUERY_TO] as Instant
      val payload = AnalyticsData(from, to, items)

      MessageBuilder
        .withPayload(payload)
        .copyHeaders(group.one.headers)
        .build()
    }
  }

  @Bean
  fun captureAnalyticsAggregationFlow(
    captureAnalyticsCorrelationStrategy: CorrelationStrategy,
    captureAnalyticsReleaseStrategy: ReleaseStrategy,
    captureAnalyticsOutputProcessor: MessageGroupProcessor
  ) = integrationFlow {
    channel(captureAnalyticsAggregateChannel)
    aggregate {
      this.correlationStrategy(captureAnalyticsCorrelationStrategy)
      this.releaseStrategy(captureAnalyticsReleaseStrategy)
      this.outputProcessor(captureAnalyticsOutputProcessor)
    }
    channel(postAnalyticsDataChannel)
  }

  @Bean
  fun postAnalyticsDataFlow() = integrationFlow {
    channel(postAnalyticsDataChannel)
    handle { data: AnalyticsData, _: MessageHeaders ->
      analyticsService.sendData(data)
        .subscribe(
          {log.info("Data was posted successfully with response: '{}'", it)},
          {log.warn("Was unable to send instance data due to: '{}'", it.localizedMessage)}
        )
    }
    channel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME)
  }

  companion object {
    const val WH_ANALYTICS_SEQ_SIZE = "WH_ANALYTICS_SEQ_SIZE"
    const val WH_ANALYTICS_SEQ_ID = "WH_ANALYTICS_SEQ_ID"
    const val WH_ANALYTICS_QUERY_FROM = "WH_ANALYTICS_QUERY_FROM"
    const val WH_ANALYTICS_QUERY_TO = "WH_ANALYTICS_QUERY_TO"
  }

/*
  @InboundChannelAdapter(value = "pollChannel", poller = [Poller(cron = EVERY_MINUTE_PATTERN)])
  fun sendAnalyticsFlow(): Message<String> {
    return MessageBuilder.withPayload("Hello").build()
  }

  @Bean
  fun source2() = integrationFlow {
    channel("pollChannel")
    handle { p: String, h: MessageHeaders ->
        println(p)
        println(h)
      }
  }
*/
}

