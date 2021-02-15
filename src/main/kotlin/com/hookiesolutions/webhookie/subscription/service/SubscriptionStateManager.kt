package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionStatus
import com.hookiesolutions.webhookie.subscription.service.model.subscription.SubscriptionStatueAction
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
    return verifyAction(subscription, SubscriptionStatus.VALIDATED, SubscriptionStatueAction.VALIDATE)
  }

  fun canBeActivated(subscription: Subscription): Mono<List<SubscriptionStatus>> {
    return verifyAction(subscription, SubscriptionStatus.ACTIVATED, SubscriptionStatueAction.ACTIVATE)
  }

  fun canBeDeactivated(subscription: Subscription): Mono<List<SubscriptionStatus>> {
    return verifyAction(subscription, SubscriptionStatus.DEACTIVATED, SubscriptionStatueAction.DEACTIVATE)
  }

  fun canBeSuspended(subscription: Subscription): Mono<List<SubscriptionStatus>> {
    return verifyAction(subscription, SubscriptionStatus.SUSPENDED, SubscriptionStatueAction.SUSPEND)
  }

  fun canBeUnsuspended(subscription: Subscription): Mono<List<SubscriptionStatus>> {
    return verifyAction(subscription, SubscriptionStatus.DEACTIVATED, SubscriptionStatueAction.UNSUSPEND)
  }

  private fun verifyAction(
    subscription: Subscription,
    status: SubscriptionStatus,
    withAction: SubscriptionStatueAction
  ): Mono<List<SubscriptionStatus>> {
    val validStatusList = validStatusListForUpdate(status, withAction)
    return if (validStatusList.contains(subscription.statusUpdate.status)) {
      validStatusList.toMono()
    } else {
      Mono.error(IllegalArgumentException("Cannot ${withAction.name.toLowerCase()} a '${subscription.statusUpdate.status}' Subscription!"))
    }
  }

  private fun validStatusListForUpdate(
    toBeStatus: SubscriptionStatus,
    action: SubscriptionStatueAction
  ): List<SubscriptionStatus> {
    return when (toBeStatus) {
      SubscriptionStatus.VALIDATED -> {
        listOf(SubscriptionStatus.SAVED, SubscriptionStatus.BLOCKED, SubscriptionStatus.DEACTIVATED)
      }
      SubscriptionStatus.ACTIVATED -> {
        listOf(SubscriptionStatus.VALIDATED, SubscriptionStatus.DEACTIVATED)
      }
      SubscriptionStatus.DEACTIVATED -> when (action) {
        SubscriptionStatueAction.DEACTIVATE -> {
          listOf(SubscriptionStatus.ACTIVATED)
        }
        SubscriptionStatueAction.UNSUSPEND -> {
          listOf(SubscriptionStatus.SUSPENDED)
        }
        else -> {
          emptyList()
        }
      }
      SubscriptionStatus.SUSPENDED -> {
        return SubscriptionStatus.values().asList()
      }
      else -> {
        return emptyList()
      }
    }
  }
}