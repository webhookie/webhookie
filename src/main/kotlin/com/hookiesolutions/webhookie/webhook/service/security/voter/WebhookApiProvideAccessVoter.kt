package com.hookiesolutions.webhookie.webhook.service.security.voter

import com.hookiesolutions.webhookie.webhook.domain.WebhookApi

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 20/1/21 13:59
 */
interface WebhookApiProvideAccessVoter {
  fun vote(webhookApi: WebhookApi, tokenGroups: Collection<String>): Boolean
}
