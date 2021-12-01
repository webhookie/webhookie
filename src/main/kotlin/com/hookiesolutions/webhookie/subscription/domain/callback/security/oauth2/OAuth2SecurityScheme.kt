package com.hookiesolutions.webhookie.subscription.domain.callback.security.oauth2

import com.hookiesolutions.webhookie.common.model.dto.security.CallbackSecuritySchemeDTO
import com.hookiesolutions.webhookie.subscription.enterprise.oauth2.model.dto.OAuth2CallbackSecuritySchemeDTO
import com.hookiesolutions.webhookie.subscription.domain.callback.security.CallbackSecurityScheme
import com.hookiesolutions.webhookie.subscription.domain.callback.security.SecuritySchemeType
import com.hookiesolutions.webhookie.subscription.domain.callback.security.oauth2.OAuth2SecurityScheme.Companion.OAUTH2_SECURITY_SCHEME_ALIAS
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document
@TypeAlias(OAUTH2_SECURITY_SCHEME_ALIAS)
data class OAuth2SecurityScheme(
  val details: OAuth2GrantTypeDetails,
  override val method: SecuritySchemeType = SecuritySchemeType.OAUTH2
): CallbackSecurityScheme() {
  override val alias: String
    get() = OAUTH2_SECURITY_SCHEME_ALIAS

  override fun dto(): CallbackSecuritySchemeDTO {
    val details = details as ClientCredentialsGrantType
    return OAuth2CallbackSecuritySchemeDTO(details.tokenEndpoint, details.clientId, details.scopes)
  }

  override fun detailsJson(): String {
    return details.json()
  }

  companion object {
    const val OAUTH2_SECURITY_SCHEME_ALIAS = "oauth2_security_scheme"
  }
}
