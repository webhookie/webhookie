package com.hookiesolutions.webhookie.subscription.domain.callback.security.oauth2

import com.hookiesolutions.webhookie.common.model.dto.security.CallbackSecuritySchemeDTO
import com.hookiesolutions.webhookie.subscription.enterprise.oauth2.model.dto.OAuth2CallbackSecuritySchemeDTO
import com.hookiesolutions.webhookie.subscription.domain.callback.security.CallbackSecurityScheme
import com.hookiesolutions.webhookie.subscription.domain.callback.security.SecuritySchemeType
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document
@TypeAlias("oauth2_security_scheme")
data class OAuth2SecurityScheme(
  val details: OAuth2GrantTypeDetails
): CallbackSecurityScheme() {
  override val method: SecuritySchemeType
    get() = SecuritySchemeType.OAUTH2

  override fun dto(): CallbackSecuritySchemeDTO {
    val details = details as ClientCredentialsGrantType
    return OAuth2CallbackSecuritySchemeDTO(details.tokenEndpoint, details.clientId, details.scopes)
  }
}
