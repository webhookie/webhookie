package com.hookiesolutions.webhookie.webhook.domain

import org.springframework.data.mongodb.core.index.Indexed

data class Topic(
  @Indexed(unique = true)
  val name: String,
  val description: String?
)