package com.hookiesolutions.webhookie.subscription.service.model.subscription

import com.hookiesolutions.webhookie.common.model.UserProfile
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionApprovalDetails

data class SubscriptionApprovalResponse(
  val reason: String,
  val email: String,
  val requester: UserProfile
) {
  companion object {
    fun create(details: SubscriptionApprovalDetails): SubscriptionApprovalResponse {
      return SubscriptionApprovalResponse(details.reason, details.email.value, details.requester)
    }
  }
}
