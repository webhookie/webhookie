package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.EmailValue
import com.hookiesolutions.webhookie.subscription.service.model.subscription.WebhookSubscriptionDetails

data class SubscriptionApprovalDetails(
  val required: Boolean,
  val reason: String? = null,
  val email: EmailValue? = null
) {
  companion object {
    fun from(details: WebhookSubscriptionDetails): SubscriptionApprovalDetails {
      return SubscriptionApprovalDetails(
        details.requiresApproval,
        details.reason,
        details.email
      )
    }
  }
}
