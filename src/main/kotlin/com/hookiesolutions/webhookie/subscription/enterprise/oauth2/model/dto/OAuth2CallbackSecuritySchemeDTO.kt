package com.hookiesolutions.webhookie.subscription.enterprise.oauth2.model.dto

import com.hookiesolutions.webhookie.common.model.dto.security.CallbackSecuritySchemeDTO
import com.hookiesolutions.webhookie.subscription.domain.callback.security.SecuritySchemeType

data class OAuth2CallbackSecuritySchemeDTO(
  private val tokenEndpoint: String,
  private val clientId: String,
  private val scopes: List<String>
): CallbackSecuritySchemeDTO() {
  override val method: SecuritySchemeType
    get() = SecuritySchemeType.OAUTH2
  override val details: OAuthCallbackSecurityDetailsDTO
    get() = OAuthCallbackSecurityDetailsDTO(tokenEndpoint, clientId, scopes)
}
