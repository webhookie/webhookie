package com.hookiesolutions.webhookie.subscription.domain

import com.bol.secure.Encrypted

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 23:22
 */
data class CallbackSecurity(
  val method: String = "HMAC",
  @Encrypted
  val secret: Secret
)

data class Secret(
  val keyId: String,
  val secret: String
)

