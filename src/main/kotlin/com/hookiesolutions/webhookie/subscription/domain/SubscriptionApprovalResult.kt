package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.UserProfile
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionStatus
import java.time.Instant

data class SubscriptionApprovalResult(
  val user: UserProfile,
  val at: Instant,
  val status: SubscriptionStatus,
  val reason: String? = null,
)
