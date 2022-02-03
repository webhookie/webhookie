package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.EmailValue
import com.hookiesolutions.webhookie.common.model.UserProfile

data class SubscriptionApprovalDetails(
  val reason: String,
  val email: EmailValue,
  val requester: UserProfile
)
