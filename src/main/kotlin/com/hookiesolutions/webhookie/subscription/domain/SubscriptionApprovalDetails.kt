package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.EmailValue

data class SubscriptionApprovalDetails(
  val reason: String,
  val email: EmailValue
)
