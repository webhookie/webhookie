package com.hookiesolutions.webhookie.webhook.domain

import org.springframework.data.mongodb.core.index.Indexed

data class Topic(
  @Indexed(name = "webhook_api.topic", unique = true)
  val name: String,
  val description: String?
) {
  class Keys {
    companion object {
      const val KEY_TOPIC_NAME = "name"
    }
  }
}
