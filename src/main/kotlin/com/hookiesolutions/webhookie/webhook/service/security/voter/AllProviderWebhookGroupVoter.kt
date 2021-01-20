package com.hookiesolutions.webhookie.webhook.service.security.voter

import com.hookiesolutions.webhookie.webhook.domain.ProviderAccess
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 20/1/21 13:59
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class AllProviderWebhookGroupVoter: WebhookGroupProvideAccessVoter {
  override fun vote(webhookGroup: WebhookGroup, tokenGroups: List<String>): Boolean {
    return webhookGroup.providerAccess == ProviderAccess.ALL
  }
}