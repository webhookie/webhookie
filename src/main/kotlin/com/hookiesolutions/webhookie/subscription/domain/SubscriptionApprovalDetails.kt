package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.UserProfile
import java.time.Instant
import javax.validation.constraints.NotBlank

data class SubscriptionApprovalDetails(
  @field:NotBlank
  val reason: String,
  val requester: UserProfile,
  val at: Instant,
  val result: SubscriptionApprovalResult? = null
) {
  class Keys {
    companion object {
      const val KEY_APPROVAL_RESULT = "result"
    }
  }
}

