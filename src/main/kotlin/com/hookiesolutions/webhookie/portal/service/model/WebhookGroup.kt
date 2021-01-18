package com.hookiesolutions.webhookie.portal.service.model

data class WebhookGroup(
  val name: String,
  val version: String,
  val description: String?,
  val topics: List<Topic>,
  val spec: AsyncApiSpec
)

data class Topic(
  val path: String,
  val description: String?
)
