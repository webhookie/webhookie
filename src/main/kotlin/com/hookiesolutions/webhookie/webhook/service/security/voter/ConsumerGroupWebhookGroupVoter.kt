package com.hookiesolutions.webhookie.webhook.service.security.voter

import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(100)
class ConsumerGroupWebhookGroupVoter: WebhookGroupConsumeAccessVoter {
  override fun vote(webhookGroup: WebhookGroup, tokenGroups: Collection<String>): Boolean {
    return tokenGroups.any {
      webhookGroup.consumerIAMGroups.contains(it)
    }
  }
}