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

package com.hookiesolutions.webhookie.analytics.config

import com.hookiesolutions.webhookie.analytics.service.AnalyticsFlows
import com.hookiesolutions.webhookie.analytics.service.model.AnalyticsTimeCriteria
import com.hookiesolutions.webhookie.common.service.TimeMachine
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.integration.core.MessageSource
import org.springframework.integration.dsl.MessageChannels
import org.springframework.integration.dsl.PollerSpec
import org.springframework.integration.dsl.Pollers
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.function.Supplier

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/8/21 15:53
 */
@Configuration
@ConditionalOnProperty(prefix = AnalyticsProperties.PROPS_ANALYTICS_PREFIX, value = ["send"], havingValue = "true", matchIfMissing = true)
class AnalyticsFlowsConfig(
  private val timeMachine: TimeMachine
) {
  @Bean
  @Profile("dev")
  fun everyMinuteAnalyticsPoller(): PollerSpec = Pollers.cron(TimeMachine.EVERY_DAY_PATTERN)

  @Bean
  @Profile("!dev")
  fun everyDayAnalyticsPoller(): PollerSpec = Pollers.cron(TimeMachine.EVERY_DAY_PATTERN)

  @Bean
  fun analyticsPollingSpec(pollerSpec: PollerSpec): SourcePollingChannelAdapterSpec.() -> Unit = {
    poller(pollerSpec)
  }

  @Bean
  @Profile("dev")
  fun devAnalyticsTimeCriteriaSupplier(): Supplier<AnalyticsTimeCriteria> {
    return Supplier {
      val to = timeMachine.now()
      val from = to.minus(1, ChronoUnit.DAYS)
      AnalyticsTimeCriteria(from, to)
    }
  }

  @Bean
  @Profile("!dev")
  fun analyticsTimeCriteriaSupplier(): Supplier<AnalyticsTimeCriteria> {
    return Supplier {
      val to = timeMachine.now()
        .truncatedTo(ChronoUnit.DAYS)
      val from = to.minus(1, ChronoUnit.DAYS)
      AnalyticsTimeCriteria(from, to)
    }
  }

  @Bean
  fun analyticsMessageSource(
    analyticsCriteriaSupplier: Supplier<AnalyticsTimeCriteria>
  ) = MessageSource {
    val analyticsTimeCriteria = analyticsCriteriaSupplier.get()
    return@MessageSource MessageBuilder
      .withPayload(analyticsTimeCriteria)
      .setHeader(AnalyticsFlows.WH_ANALYTICS_SEQ_ID, timeMachine.now())
      .setHeader(AnalyticsFlows.WH_ANALYTICS_SEQ_SIZE, 3)
      .setHeader(AnalyticsFlows.WH_ANALYTICS_QUERY_FROM, analyticsTimeCriteria.from)
      .setHeader(AnalyticsFlows.WH_ANALYTICS_QUERY_TO, analyticsTimeCriteria.to)
      .build()
  }

  @Bean
  fun captureAnalyticsTracesChannel(): MessageChannel = MessageChannels
    .executor(Executors.newSingleThreadExecutor())
    .get()

  @Bean
  fun captureAnalyticsSpansChannel(): MessageChannel = MessageChannels
    .executor(Executors.newSingleThreadExecutor())
    .get()

  @Bean
  fun captureAnalyticsSubscriptionsChannel(): MessageChannel = MessageChannels
    .executor(Executors.newSingleThreadExecutor())
    .get()

  @Bean
  fun captureAnalyticsAggregateChannel(): MessageChannel = MessageChannels
    .executor(Executors.newSingleThreadExecutor())
    .get()

  @Bean
  fun postAnalyticsDataChannel(): MessageChannel = MessageChannels
    .executor(Executors.newSingleThreadExecutor())
    .get()
}
