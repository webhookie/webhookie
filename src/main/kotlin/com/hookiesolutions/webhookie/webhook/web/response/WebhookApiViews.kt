package com.hookiesolutions.webhookie.webhook.web.response

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 25/1/21 13:20
 */
interface WebhookApiViews {
  open class Summary

  open class Full: Summary()
}
