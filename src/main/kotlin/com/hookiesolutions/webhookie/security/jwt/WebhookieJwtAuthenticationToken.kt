package com.hookiesolutions.webhookie.security.jwt

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

data class WebhookieJwtAuthenticationToken(
  val jwt: Jwt,
  val roles: Collection<GrantedAuthority>,
  val groups: List<String>,
  val subject: String,
  val entity: String
): JwtAuthenticationToken(jwt, roles)
