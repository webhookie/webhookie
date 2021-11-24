package com.hookiesolutions.webhookie.subscription.domain.callback.security

import com.bol.secure.Encrypted
import com.hookiesolutions.webhookie.common.model.dto.CallbackSecurityDTO
import com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac.HmacSecret

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 23:22
 */
data class CallbackSecurity(
  val method: String = "HMAC",
  @Encrypted
  val secret: HmacSecret
) {
  fun dto(): CallbackSecurityDTO {
    return CallbackSecurityDTO(method, secret.keyId)
  }
}
