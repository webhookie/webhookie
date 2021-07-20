package com.hookiesolutions.webhookie.webhook.config

import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 20:09
 */
@Configuration
class WebhookApiAPIDocs {
  @Bean
  fun webhookOpenApi(): GroupedOpenApi {
    val paths = arrayOf("${REQUEST_MAPPING_WEBHOOK_APIS}/**")
    return GroupedOpenApi
      .builder()
      .group("Webhook Apis")
      .pathsToMatch(*paths)
      .build()
  }

  companion object {
    const val REQUEST_MAPPING_WEBHOOK_APIS = "/webhookapis"
  }
}
