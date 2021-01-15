package com.hookiesolutions.webhookie.config.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "webhookie.security")
data class WebHookieSecurityProperties(
  val audience: String,
  val loginUrl: String,
  val roles: RolesConfig,
  val groups: PathConfig,
  val email: PathConfig,
  val oauth2: OAuth2Properties,
  val noAuth: NoAuth = NoAuth()
) {
  data class NoAuth(
    val pathMatchers: Map<String,Array<String>> = mapOf()
  )

  data class RolesConfig(
    val jwkJsonPath: String,
    val autoAssignConsumer: Boolean = true,
    val roleMapping: Map<String, String> = mapOf()
  )

  data class PathConfig(
    val jwkJsonPath: String
  )

  data class OAuth2Properties(
    val authorizationUri: String,
    val tokenUri: String,
    val tokenName: String = "access_token"
  )
}
