package com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac

import com.hookiesolutions.webhookie.common.model.dto.security.CallbackSecuritySchemeDTO
import com.hookiesolutions.webhookie.common.model.dto.security.hmac.HmacCallbackSecuritySchemeDTO
import com.hookiesolutions.webhookie.subscription.domain.callback.security.CallbackSecurityScheme
import com.hookiesolutions.webhookie.subscription.domain.callback.security.SecuritySchemeType
import com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac.HmacSecurityScheme.Companion.HMAC_SECURITY_SCHEME_ALIAS
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document
@TypeAlias(HMAC_SECURITY_SCHEME_ALIAS)
data class HmacSecurityScheme(
  val details: HmacDetails,
  override val method: SecuritySchemeType = SecuritySchemeType.HMAC,
): CallbackSecurityScheme() {
  override val alias: String
    get() = HMAC_SECURITY_SCHEME_ALIAS


  override fun dto(): CallbackSecuritySchemeDTO {
    return HmacCallbackSecuritySchemeDTO(details.keyId)
  }

  override fun detailsJson(): String {
    return details.json()
  }

  companion object {
    const val HMAC_SECURITY_SCHEME_ALIAS = "hmac_security_scheme"
  }
}
