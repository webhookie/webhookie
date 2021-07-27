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

package com.hookiesolutions.webhookie.security.service

import com.hookiesolutions.webhookie.security.TokenData
import com.hookiesolutions.webhookie.security.jwt.WebhookieJwtAuthenticationToken
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 19:36
 */
@Component
class SecurityHandler {
  fun token(): Mono<WebhookieJwtAuthenticationToken> {
    return ReactiveSecurityContextHolder.getContext()
      .map(SecurityContext::getAuthentication)
      .filter(Authentication::isAuthenticated)
      .cast(WebhookieJwtAuthenticationToken::class.java)
  }

  fun data(): Mono<TokenData> {
    return token()
      .switchIfEmpty { AccessDeniedException("Token is not provided").toMono() }
      .map { TokenData(it.entity, it.groups, it.roles, it.subject) }
  }

  fun groups(): Mono<List<String>> {
    return token()
      .map { it.groups }
  }

  fun entity(): Mono<String> {
    return token()
      .switchIfEmpty { AccessDeniedException("Token is not provided").toMono() }
      .map { it.entity }
  }
}
