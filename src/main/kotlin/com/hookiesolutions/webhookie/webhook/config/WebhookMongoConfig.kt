package com.hookiesolutions.webhookie.webhook.config

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 14:10
 */
@Configuration
class WebhookMongoConfig {
  @Bean
  fun webhookIndexEntities(): List<Class<out AbstractEntity>> =
    listOf(WebhookGroup::class.java)
}
