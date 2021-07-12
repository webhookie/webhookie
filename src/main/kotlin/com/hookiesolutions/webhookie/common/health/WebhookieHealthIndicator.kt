package com.hookiesolutions.webhookie.common.health

import com.hookiesolutions.webhookie.consumer.config.ConsumerProperties
import com.hookiesolutions.webhookie.security.WebHookieSecurityProperties
import com.hookiesolutions.webhookie.subscription.config.SubscriptionProperties
import com.hookiesolutions.webhookie.webhook.config.parser.ParserServiceProperties
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/7/21 15:35
 */
@Component
class WebhookieHealthIndicator(
  private val parserServiceProperties: ParserServiceProperties,
  private val webHookieSecurityProperties: WebHookieSecurityProperties,
  private val subscriptionProperties: SubscriptionProperties,
  private val consumerProperties: ConsumerProperties
): ReactiveHealthIndicator {
  override fun health(): Mono<Health> {
    return Health
      .up()
      .withDetail("parser", parserServiceProperties)
      .withDetail("consumer", consumerProperties)
      .withDetail("security", webHookieSecurityProperties)
      .withDetail("subscription", subscriptionProperties)
      .build()
      .toMono()
  }
}
