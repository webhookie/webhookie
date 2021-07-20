package com.hookiesolutions.webhookie.webhook.service.security.voter

import com.hookiesolutions.webhookie.webhook.domain.WebhookApi
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(100)
class ConsumerGroupWebhookApiVoter: WebhookApiConsumeAccessVoter {
  override fun vote(webhookApi: WebhookApi, tokenGroups: Collection<String>): Boolean {
    return tokenGroups.any {
      webhookApi.consumerIAMGroups.contains(it)
    }
  }
}
