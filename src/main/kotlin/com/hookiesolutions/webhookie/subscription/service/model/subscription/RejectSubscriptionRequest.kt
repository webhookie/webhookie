package com.hookiesolutions.webhookie.subscription.service.model.subscription

data class RejectSubscriptionRequest(
  val user: UserProfileRequest,
  val reason: String
)
