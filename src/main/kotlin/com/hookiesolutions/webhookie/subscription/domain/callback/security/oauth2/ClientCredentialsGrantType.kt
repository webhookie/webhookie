package com.hookiesolutions.webhookie.subscription.domain.callback.security.oauth2

import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@TypeAlias("oauth2_client_credentials")
@Document
data class ClientCredentialsGrantType(
  val tokenEndpoint: String,
  val clientId: String,
  val secret: String,
): OAuth2GrantTypeDetails() {
  override val type: OAuth2GrantType
    get() = OAuth2GrantType.CLIENT_CREDENTIALS
}
