/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "webhookie.security")
data class WebHookieSecurityProperties(
  val audience: String,
  val clientId: String,
  val roles: RolesConfig,
  val groups: PathConfig,
  val entity: PathConfig,
  val oauth2: OAuth2Properties,
  val noAuth: NoAuth = NoAuth(),
  val allowedOrigins: List<String> = emptyList()
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
