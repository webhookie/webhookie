package com.hookiesolutions.webhookie.webhook.service.security.voter

import com.hookiesolutions.webhookie.webhook.domain.ConsumerAccess
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class PubliclyConsumableWebhookGroupVoter : WebhookGroupConsumeAccessVoter {
  override fun vote(webhookGroup: WebhookGroup, tokenGroups: List<String>): Boolean {
    return webhookGroup.consumerAccess == ConsumerAccess.PUBLIC
  }
}