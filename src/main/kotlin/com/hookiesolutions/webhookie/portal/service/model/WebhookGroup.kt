package com.hookiesolutions.webhookie.portal.service.model

data class WebhookGroup(
  val name: String,
  val version: String,
  val description: String?,
  val topics: List<String>,
  val spec: AsyncApiSpec
)