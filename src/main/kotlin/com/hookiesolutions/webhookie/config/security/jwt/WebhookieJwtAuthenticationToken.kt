package com.hookiesolutions.webhookie.config.security.jwt

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

data class WebhookieJwtAuthenticationToken(
  val jwt: Jwt,
  val roles: Collection<GrantedAuthority>,
  val groups: List<String>
): JwtAuthenticationToken(jwt, roles)