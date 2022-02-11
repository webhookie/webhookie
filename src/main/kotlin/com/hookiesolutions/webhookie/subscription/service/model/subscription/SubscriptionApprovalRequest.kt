package com.hookiesolutions.webhookie.subscription.service.model.subscription

import com.hookiesolutions.webhookie.common.model.UserProfile
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionApprovalDetails
import javax.validation.constraints.NotBlank

data class SubscriptionApprovalRequest(
  @field:NotBlank
  val reason: String,
  val requester: UserProfile
) {
  fun toSubscriptionApprovalDetails(): SubscriptionApprovalDetails {
    return SubscriptionApprovalDetails(reason, requester)
  }
}
