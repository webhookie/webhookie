package com.hookiesolutions.webhookie.common.health

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
class WebhookieHealthIndicator: ReactiveHealthIndicator {
  override fun health(): Mono<Health> {
    return Health.up().build().toMono()
  }
}
