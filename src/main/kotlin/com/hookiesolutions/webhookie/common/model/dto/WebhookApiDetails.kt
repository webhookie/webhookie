package com.hookiesolutions.webhookie.common.model.dto

data class WebhookApiDetails(
  val requiresApproval: Boolean,
  val topics: List<String>
)
