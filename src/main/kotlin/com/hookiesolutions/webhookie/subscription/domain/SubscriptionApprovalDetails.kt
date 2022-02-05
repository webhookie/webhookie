package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.EmailValue
import com.hookiesolutions.webhookie.common.model.UserProfile

data class SubscriptionApprovalDetails(
  val reason: String,
  val email: EmailValue,
  val requester: UserProfile,
  val result: SubscriptionApprovalResult? = null
) {
  class Keys {
    companion object {
      const val KEY_APPROVAL_RESULT = "result"
    }
  }
}

