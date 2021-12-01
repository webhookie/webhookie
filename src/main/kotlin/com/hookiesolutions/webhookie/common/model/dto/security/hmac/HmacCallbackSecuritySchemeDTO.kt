package com.hookiesolutions.webhookie.common.model.dto.security.hmac

import com.hookiesolutions.webhookie.common.model.dto.security.CallbackSecuritySchemeDTO
import com.hookiesolutions.webhookie.subscription.domain.callback.security.SecuritySchemeType

data class HmacCallbackSecuritySchemeDTO(
  private val keyId: String
): CallbackSecuritySchemeDTO() {
  override val method: SecuritySchemeType
    get() = SecuritySchemeType.HMAC
  override val details: HmacCallbackSecurityDetailsDTO
    get() = HmacCallbackSecurityDetailsDTO(keyId)
}
