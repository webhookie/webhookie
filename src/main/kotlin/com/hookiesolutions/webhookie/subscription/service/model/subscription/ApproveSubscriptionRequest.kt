package com.hookiesolutions.webhookie.subscription.service.model.subscription

import com.hookiesolutions.webhookie.common.model.UserProfile

data class ApproveSubscriptionRequest(
  val user: UserProfile
)
