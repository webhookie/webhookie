package com.hookiesolutions.webhookie.subscription.domain.callback.security.oauth2

import com.hookiesolutions.webhookie.subscription.domain.callback.security.CallbackSecurityScheme
import com.hookiesolutions.webhookie.subscription.domain.callback.security.SecuritySchemeType
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document
@TypeAlias("oauth2_security_scheme")
data class OAuth2SecurityScheme(
  val grantTypeDetails: OAuth2GrantTypeDetails
): CallbackSecurityScheme() {
  override val type: SecuritySchemeType
    get() = SecuritySchemeType.OAUTH2
}
