package com.hookiesolutions.webhookie.webhook.service.security.voter

import com.hookiesolutions.webhookie.webhook.domain.WebhookApi
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class ProviderGroupWebhookApiVoter: WebhookApiConsumeAccessVoter, WebhookApiProvideAccessVoter {
  override fun vote(webhookApi: WebhookApi, tokenGroups: Collection<String>): Boolean {
    return tokenGroups.any {
      webhookApi.providerIAMGroups.contains(it)
    }
  }
}
