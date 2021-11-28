package com.hookiesolutions.webhookie.subscription.domain.callback.security.oauth2

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.hookiesolutions.webhookie.subscription.domain.callback.security.oauth2.OAuth2GrantTypeDetails.Companion.PROPERTY_TYPE

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = PROPERTY_TYPE)
@JsonSubTypes(
  JsonSubTypes.Type(value = ClientCredentialsGrantType::class, name = "CLIENT_CREDENTIALS"),
)
abstract class OAuth2GrantTypeDetails {
  abstract val type: OAuth2GrantType

  companion object {
    const val PROPERTY_TYPE = "type"
  }
}
