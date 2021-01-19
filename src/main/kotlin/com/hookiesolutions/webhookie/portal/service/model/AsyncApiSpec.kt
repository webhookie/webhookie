package com.hookiesolutions.webhookie.portal.service.model

import com.hookiesolutions.webhookie.portal.domain.Topic

data class AsyncApiSpec(
  val name: String,
  val version: String,
  val description: String?,
  val topics: List<Topic>,
  val raw: String,
  val spec: Map<String, Any>
)