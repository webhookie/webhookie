package com.hookiesolutions.webhookie.subscription.domain.callback.security

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.hookiesolutions.webhookie.common.model.dto.security.CallbackSecuritySchemeDTO
import com.hookiesolutions.webhookie.subscription.domain.callback.security.CallbackSecurityScheme.Companion.PROPERTY_KEY
import com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac.HmacSecurityScheme
import com.hookiesolutions.webhookie.subscription.domain.callback.security.oauth2.OAuth2SecurityScheme

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = PROPERTY_KEY)
@JsonSubTypes(
  JsonSubTypes.Type(value = HmacSecurityScheme::class, name = "HMAC"),
  JsonSubTypes.Type(value = OAuth2SecurityScheme::class, name = "OAUTH2")
)
abstract class CallbackSecurityScheme {
  abstract val method: SecuritySchemeType
  abstract fun dto(): CallbackSecuritySchemeDTO?

  fun isHmac(): Boolean = method == SecuritySchemeType.HMAC

  companion object {
    const val PROPERTY_KEY = "method"

    fun isHmac(scheme: CallbackSecurityScheme?): Boolean {
      if(scheme == null) {
        return false
      }

      return scheme.isHmac()
    }
  }
}
