package com.hookiesolutions.webhookie.subscription.service.state

import com.hookiesolutions.webhookie.common.model.dto.SubscriptionStatus
import com.hookiesolutions.webhookie.subscription.domain.Subscription

data class SubscriptionStatusUpdate(
  val subscription: Subscription,
  val validStatusList: List<SubscriptionStatus>,
  val toBe: SubscriptionStatus
)
