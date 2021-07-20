package com.hookiesolutions.webhookie.webhook.service.security.voter

import com.hookiesolutions.webhookie.webhook.domain.ConsumerAccess
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class PubliclyConsumableWebhookApiVoter : WebhookApiConsumeAccessVoter {
  override fun vote(webhookApi: WebhookApi, tokenGroups: Collection<String>): Boolean {
    return webhookApi.consumerAccess == ConsumerAccess.PUBLIC
  }
}
