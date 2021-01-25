package com.hookiesolutions.webhookie.webhook.service.security.voter

import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 20/1/21 13:59
 */
interface WebhookGroupProvideAccessVoter {
  fun vote(webhookGroup: WebhookGroup, tokenGroups: Collection<String>): Boolean
}