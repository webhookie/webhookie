package com.hookiesolutions.webhookie.config.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "webhookie.security")
class WebHookieSecurityProperties(
  val audience: String,
  val loginUrl: String,
  val roles: RolesConfig,
  val noAuth: NoAuth = NoAuth()
)

@ConstructorBinding
@ConfigurationProperties(prefix = "webhookie.security.no-auth")
data class NoAuth(
  val pathMatchers: Map<String,Array<String>> = mapOf()
)

@ConstructorBinding
@ConfigurationProperties(prefix = "webhookie.security.roles")
data class RolesConfig(
  val jwkJsonPath: String,
  val autoAssignConsumer: Boolean = true,
  val roleMapping: Map<String, String> = mapOf()
)
