package com.hookiesolutions.webhookie.subscription.service.model.subscription

import com.hookiesolutions.webhookie.common.model.UserProfile

data class RejectSubscriptionRequest(
  val user: UserProfile,
  val reason: String
)
