package com.hookiesolutions.webhookie.subscription.domain.callback.security.oauth2

import com.hookiesolutions.webhookie.subscription.domain.callback.security.oauth2.ClientCredentialsGrantType.Companion.OAUTH2_CLIENT_CREDENTIALS_ALIAS
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@TypeAlias(OAUTH2_CLIENT_CREDENTIALS_ALIAS)
@Document
data class ClientCredentialsGrantType(
  val tokenEndpoint: String,
  val clientId: String,
  val secret: String,
  val scopes: List<String>
): OAuth2GrantTypeDetails() {
  override val type: OAuth2GrantType
    get() = OAuth2GrantType.CLIENT_CREDENTIALS

  override fun json(): String {
    return """
      {
        "tokenEndpoint": "$tokenEndpoint",
        "clientId": "$clientId",
        "secret": "$secret",
        "scopes": "$scopes"
        "_class": ""$OAUTH2_CLIENT_CREDENTIALS_ALIAS
      }
    """.trimIndent()
  }

  companion object {
    const val OAUTH2_CLIENT_CREDENTIALS_ALIAS = "oauth2_client_credentials"
  }
}
