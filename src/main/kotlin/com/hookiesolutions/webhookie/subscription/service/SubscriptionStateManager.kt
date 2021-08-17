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

package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionStatus
import com.hookiesolutions.webhookie.subscription.service.model.subscription.SubscriptionStatueAction
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*

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
      Mono.error(IllegalArgumentException("Cannot ${withAction.name.lowercase(Locale.getDefault())} a '${subscription.statusUpdate.status}' Subscription!"))
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

  fun canBeDeleted(subscription: Subscription): Mono<Subscription> {
    return if(subscription.statusUpdate.status != SubscriptionStatus.ACTIVATED) {
      subscription.toMono()
    } else {
      Mono.empty()
    }
  }
}
