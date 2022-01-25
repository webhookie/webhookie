package com.hookiesolutions.webhookie.webhook.domain

data class WebhookApiApprovalDetails(
  val required: Boolean = false
) {
  companion object {
    val ALLOW_ALL = WebhookApiApprovalDetails(false)
  }
}
