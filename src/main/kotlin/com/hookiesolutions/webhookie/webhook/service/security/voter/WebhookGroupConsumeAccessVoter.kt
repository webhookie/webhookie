package com.hookiesolutions.webhookie.webhook.service.security.voter

import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 20/1/21 12:04
 */
interface WebhookGroupConsumeAccessVoter {
  fun vote(webhookGroup: WebhookGroup, tokenGroups: List<String>): Boolean
}
