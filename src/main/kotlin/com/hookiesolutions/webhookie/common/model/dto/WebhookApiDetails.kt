package com.hookiesolutions.webhookie.common.model.dto

import com.hookiesolutions.webhookie.webhook.domain.WebhookApiApprovalDetails

data class WebhookApiDetails(
  val approvalDetails: WebhookApiApprovalDetails,
  val topics: List<String>
)
