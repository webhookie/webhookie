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
    return verifyAction(
      subscription,
      listOf(SubscriptionStatus.SAVED, SubscriptionStatus.BLOCKED, SubscriptionStatus.DEACTIVATED),
      SubscriptionStatus.VALIDATED
    )
  }

  fun canBeActivated(subscription: Subscription): Mono<Subscription> {
    return verifyAction(
      subscription,
      listOf(SubscriptionStatus.VALIDATED, SubscriptionStatus.DEACTIVATED),
      SubscriptionStatus.ACTIVATED
    )
  }

  fun canBeDeactivated(subscription: Subscription): Mono<Subscription> {
    return verifyAction(
      subscription,
      listOf(SubscriptionStatus.ACTIVATED),
      SubscriptionStatus.DEACTIVATED
    )
  }

  fun verifyAction(
    subscription: Subscription,
    validStatusList: List<SubscriptionStatus>,
    status: SubscriptionStatus
  ): Mono<Subscription> {
    return if (validStatusList.contains(subscription.statusUpdate.status)) {
      subscription.toMono()
    } else {
      Mono.error(IllegalArgumentException("'${subscription.statusUpdate.status}' Subscription cannot be ${status.name.toLowerCase()}!"))
    }
  }
}