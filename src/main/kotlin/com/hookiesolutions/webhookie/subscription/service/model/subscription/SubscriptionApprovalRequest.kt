package com.hookiesolutions.webhookie.subscription.service.model.subscription

import com.hookiesolutions.webhookie.common.model.UserProfile
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionApprovalDetails
import java.time.Instant
import javax.validation.constraints.NotBlank

data class SubscriptionApprovalRequest(
  @field:NotBlank
  val reason: String,
  val requester: UserProfileRequest
) {
  fun toSubscriptionApprovalDetails(at: Instant, userId: String): SubscriptionApprovalDetails {
    return SubscriptionApprovalDetails(reason, UserProfile(userId, requester.name, requester.email), at)
  }
}
