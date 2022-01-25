package com.hookiesolutions.webhookie.webhook.domain

import com.hookiesolutions.webhookie.common.model.EmailValue

data class WebhookApiApprovalDetails(
  val required: Boolean = false,
  val email: EmailValue? = null
) {
  companion object {
    val ALLOW_ALL = WebhookApiApprovalDetails(false)
  }
}
