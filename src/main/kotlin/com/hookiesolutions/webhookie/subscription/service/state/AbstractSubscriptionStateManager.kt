package com.hookiesolutions.webhookie.subscription.service.state

import com.hookiesolutions.webhookie.common.model.dto.SubscriptionStatus

abstract class AbstractSubscriptionStateManager {
  protected val basicStatusTransitionMap: Map<SubscriptionStatus, List<SubscriptionStatus>> = mapOf(
    SubscriptionStatus.DEACTIVATED to listOf(SubscriptionStatus.ACTIVATED),
    SubscriptionStatus.UNSUSPENDED to listOf(SubscriptionStatus.SUSPENDED),
    SubscriptionStatus.SUSPENDED to listOf(
      SubscriptionStatus.ACTIVATED,
      SubscriptionStatus.BLOCKED
    )
  )
  abstract val statusTransitionMap: Map<SubscriptionStatus, List<SubscriptionStatus>>
}
