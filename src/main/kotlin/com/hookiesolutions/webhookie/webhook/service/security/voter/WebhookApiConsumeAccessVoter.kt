package com.hookiesolutions.webhookie.webhook.service.security.voter

import com.hookiesolutions.webhookie.webhook.domain.WebhookApi

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 20/1/21 12:04
 */
interface WebhookApiConsumeAccessVoter {
  fun vote(webhookApi: WebhookApi, tokenGroups: Collection<String>): Boolean
}
