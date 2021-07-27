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

package com.hookiesolutions.webhookie.common.health

import com.hookiesolutions.webhookie.common.exception.RemoteServiceException
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 9/7/21 00:21
 */
@Component
class IAMHealthIndicator(
  private val jwtKeySetWebClient: WebClient,
  private val resourceServerProperties: OAuth2ResourceServerProperties
): ReactiveHealthIndicator {
  override fun health(): Mono<Health> {
    return jwtKeySetWebClient
      .get()
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(Any::class.java)
      .flatMap { up() }
      .onErrorResume(WebClientRequestException::class.java) {
        RemoteServiceException("IdP service is either starting up or not available").toMono()
      }
      .onErrorResume { down() }
  }

  private fun up(): Mono<Health> {
    return Health
      .up()
      .withDetail("jwt", resourceServerProperties.jwt)
      .build()
      .toMono()
  }

  private fun down(): Mono<Health> {
    return         Health
      .down()
      .withDetail("error", "IdP service is either starting up or not available")
      .build()
      .toMono()
  }
}

