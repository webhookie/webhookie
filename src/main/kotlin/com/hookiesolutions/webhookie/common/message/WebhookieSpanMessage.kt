package com.hookiesolutions.webhookie.common.message

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 11/3/21 10:13
 */
interface WebhookieSpanMessage: WebhookieMessage {
  val spanId: String
}
