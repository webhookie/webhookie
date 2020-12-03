package com.hookiesolutions.webhookie.subscription.domain

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:31
 */
data class CallbackSecurity(
  val method: String = "HMAC",
  val secret: String
)
