package com.hookiesolutions.webhookie.subscription.domain.callback.security

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.hookiesolutions.webhookie.subscription.domain.callback.security.CallbackSecurityScheme.Companion.ATTRIBUTE_TYPE
import com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac.HmacSecurityScheme
import com.hookiesolutions.webhookie.subscription.domain.callback.security.oauth2.OAuth2SecurityScheme

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = ATTRIBUTE_TYPE)
@JsonSubTypes(
  JsonSubTypes.Type(value = NoneSecurityScheme::class, name = "NONE"),
  JsonSubTypes.Type(value = HmacSecurityScheme::class, name = "HMAC"),
  JsonSubTypes.Type(value = OAuth2SecurityScheme::class, name = "OAUTH2")
)
@JsonIgnoreProperties(
  ATTRIBUTE_TYPE
)
abstract class CallbackSecurityScheme {
  abstract val type: SecuritySchemeType

  companion object {
    const val ATTRIBUTE_TYPE = "type"
  }
}
