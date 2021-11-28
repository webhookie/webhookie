package com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac

import com.hookiesolutions.webhookie.common.model.dto.CallbackSecuritySchemeDTO
import com.hookiesolutions.webhookie.common.model.dto.HmacCallbackSecuritySchemeDTO
import com.hookiesolutions.webhookie.subscription.domain.callback.security.CallbackSecurityScheme
import com.hookiesolutions.webhookie.subscription.domain.callback.security.SecuritySchemeType
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document
@TypeAlias("hmac_security_scheme")
data class HmacSecurityScheme(
  val details: HmacDetails
): CallbackSecurityScheme() {
  override val method: SecuritySchemeType
    get() = SecuritySchemeType.HMAC

  override fun dto(): CallbackSecuritySchemeDTO {
    return HmacCallbackSecuritySchemeDTO(details.keyId)
  }
}
