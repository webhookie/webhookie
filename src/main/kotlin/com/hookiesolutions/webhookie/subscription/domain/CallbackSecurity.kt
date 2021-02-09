package com.hookiesolutions.webhookie.subscription.domain

import com.bol.secure.Encrypted
import com.hookiesolutions.webhookie.common.model.dto.CallbackSecurityDTO

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 23:22
 */
data class CallbackSecurity(
  val method: String = "HMAC",
  @Encrypted
  val secret: Secret
) {
  fun dto(): CallbackSecurityDTO {
    return CallbackSecurityDTO(method, secret.keyId)
  }
}

data class Secret(
  val keyId: String,
  val secret: String
) {
  fun json(): String {
    return """
      {
        keyId: '$keyId',
        secret: '$secret'
      }
    """.trimIndent()
  }
}

