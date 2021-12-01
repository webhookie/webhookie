package com.hookiesolutions.webhookie.common.model.dto.security

import com.fasterxml.jackson.annotation.JsonIgnore
import com.hookiesolutions.webhookie.subscription.domain.callback.security.SecuritySchemeType

abstract class CallbackSecuritySchemeDTO {
  abstract val method: SecuritySchemeType
  abstract val details: CallbackSecurityDetailsDTO

  @JsonIgnore
  fun isHmac(): Boolean {
    return method == SecuritySchemeType.HMAC
  }

  @JsonIgnore
  fun isOAuth(): Boolean {
    return method == SecuritySchemeType.OAUTH2
  }
}
