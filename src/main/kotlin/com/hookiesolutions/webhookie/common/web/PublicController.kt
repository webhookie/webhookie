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

package com.hookiesolutions.webhookie.common.web

import com.hookiesolutions.webhookie.common.web.CommonAPIDocs.Companion.REQUEST_MAPPING_PUBLIC
import com.hookiesolutions.webhookie.security.WebHookieSecurityProperties
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 18/2/21 21:51
 */
@RestController
@RequestMapping(REQUEST_MAPPING_PUBLIC)
class PublicController(
  private val securityProperties: WebHookieSecurityProperties,
  private val oAuth2ResourceServerProperties: OAuth2ResourceServerProperties
) {
  @GetMapping(
    "/config",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun iamConfig(): Mono<WebhookieConfig> {
    return WebhookieConfig.builder()
      .issuer(oAuth2ResourceServerProperties.jwt.issuerUri)
      .clientId(securityProperties.clientId)
      .audience(securityProperties.audience)
      .build()
      .toMono()
  }

}

data class WebhookieConfig(
  val iam: IAMConfig
) {
  data class IAMConfig(
    val issuer: String,
    val clientId: String,
    val audience: String
  )

  companion object {
    fun builder() = Builder()
  }

  class Builder {
    private lateinit var issuer: String
    private lateinit var clientId: String
    private lateinit var audience: String

    fun issuer(value: String) = apply { this.issuer = value }
    fun clientId(value: String) = apply { this.clientId = value }
    fun audience(value: String) = apply { this.audience = value }

    fun build() = WebhookieConfig(
      IAMConfig(issuer, clientId, audience)
    )
  }
}
