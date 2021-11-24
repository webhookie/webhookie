package com.hookiesolutions.webhookie.subscription.domain.callback.security.oauth2

@Suppress("unused")
enum class OAuth2GrantType {
  AUTHORIZATION_CODE,
  IMPLICIT,
  RESOURCE_OWNER_CREDENTIALS,
  CLIENT_CREDENTIALS,
  REFRESH_TOKEN
}
