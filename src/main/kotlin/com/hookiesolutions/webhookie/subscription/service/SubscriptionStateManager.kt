package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionStatus
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 11/2/21 14:50
 */
@Service
class SubscriptionStateManager {
  fun canBeValidated(subscription: Subscription): Mono<Subscription> {
    val canBeValidated = listOf(SubscriptionStatus.SAVED, SubscriptionStatus.BLOCKED, SubscriptionStatus.DEACTIVATED)
      .contains(subscription.statusUpdate.status)
    return if (canBeValidated) {
      subscription.toMono()
    } else {
      Mono.error(IllegalArgumentException("'${subscription.statusUpdate.status}' Subscription cannot be validated!"))
    }

  }
}