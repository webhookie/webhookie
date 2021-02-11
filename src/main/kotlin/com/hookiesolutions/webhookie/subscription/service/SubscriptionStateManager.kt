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
  fun canBeValidated(subscription: Subscription): Mono<List<SubscriptionStatus>> {
    return verifyAction(subscription, SubscriptionStatus.VALIDATED)
  }

  fun canBeActivated(subscription: Subscription): Mono<List<SubscriptionStatus>> {
    return verifyAction(subscription, SubscriptionStatus.ACTIVATED)
  }

  fun canBeDeactivated(subscription: Subscription): Mono<List<SubscriptionStatus>> {
    return verifyAction(subscription, SubscriptionStatus.DEACTIVATED)
  }

  fun canBeSuspended(subscription: Subscription): Mono<List<SubscriptionStatus>> {
    return verifyAction(subscription, SubscriptionStatus.SUSPENDED)
  }

  private fun validStatusListForUpdate(toBeStatus: SubscriptionStatus): List<SubscriptionStatus> {
    return when (toBeStatus) {
      SubscriptionStatus.VALIDATED -> {
        listOf(SubscriptionStatus.SAVED, SubscriptionStatus.BLOCKED, SubscriptionStatus.DEACTIVATED)
      }
      SubscriptionStatus.ACTIVATED -> {
        listOf(SubscriptionStatus.VALIDATED, SubscriptionStatus.DEACTIVATED)
      }
      SubscriptionStatus.DEACTIVATED -> {
        listOf(SubscriptionStatus.ACTIVATED)
      }
      SubscriptionStatus.SUSPENDED -> {
        return SubscriptionStatus.values().asList()
      }
      else -> {
        return emptyList()
      }
    }
  }

  private fun verifyAction(
    subscription: Subscription,
    status: SubscriptionStatus
  ): Mono<List<SubscriptionStatus>> {
    val validStatusList = validStatusListForUpdate(status)
    return if (validStatusList.contains(subscription.statusUpdate.status)) {
      validStatusList.toMono()
    } else {
      Mono.error(IllegalArgumentException("'${subscription.statusUpdate.status}' Subscription cannot be ${status.name.toLowerCase()}!"))
    }
  }
}