package com.hookiesolutions.webhookie.subscription.enterprise.oauth2.model.dto

import com.hookiesolutions.webhookie.common.model.dto.security.CallbackSecurityDetailsDTO

data class OAuthCallbackSecurityDetailsDTO(
  val tokenEndpoint: String,
  val clientId: String,
  val scopes: List<String>,
): CallbackSecurityDetailsDTO


