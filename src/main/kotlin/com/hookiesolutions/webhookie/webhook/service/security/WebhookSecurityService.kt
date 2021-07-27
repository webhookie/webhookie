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

package com.hookiesolutions.webhookie.webhook.service.security

import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi
import com.hookiesolutions.webhookie.webhook.service.security.voter.WebhookApiConsumeAccessVoter
import com.hookiesolutions.webhookie.webhook.service.security.voter.WebhookApiProvideAccessVoter
import org.slf4j.Logger
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.function.BiFunction
import java.util.function.Supplier

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 20/1/21 00:50
 */
@Component
class WebhookSecurityService(
  private val securityHandler: SecurityHandler,
  private val consumerAccessVoters: List<WebhookApiConsumeAccessVoter>,
  private val providerAccessVoters: List<WebhookApiProvideAccessVoter>,
  private val log: Logger
) {
  fun tokenGroups(): Mono<List<String>> {
    return securityHandler.groups()
  }

  fun verifyReadAccess(webhookApiSupplier: Supplier<Mono<WebhookApi>>): Mono<WebhookApi> {
    return verifyAccess(webhookApiSupplier, this::webhookApiIsReadableFor)
  }

  fun verifyWriteAccess(webhookApiSupplier: Supplier<Mono<WebhookApi>>): Mono<WebhookApi> {
    return verifyAccess(webhookApiSupplier, this::webhookApiIsWritableFor)
  }

  private fun verifyAccess(
    webhookApiSupplier: Supplier<Mono<WebhookApi>>,
    verifier: BiFunction<WebhookApi, Collection<String>, Boolean>
  ): Mono<WebhookApi> {
    return tokenGroups()
      .zipWith(webhookApiSupplier.get())
      .flatMap {
        val webhookApi = it.t2
        val tokenGroups = it.t1
        return@flatMap if (verifier.apply(webhookApi, tokenGroups)) {
          webhookApi.toMono()
        } else {
          if (log.isDebugEnabled) {
            log.debug("Access is denied for current user to read WebhookApi: '{}'", webhookApi.title)
          }
          Mono.error(AccessDeniedException("Access Denied!"))
        }
      }
  }

  fun webhookApiIsReadableFor(webhookApi: WebhookApi, tokenGroups: Collection<String>): Boolean {
    if (log.isDebugEnabled) {
      log.debug(
        "Checking WebhookApi '{}', '{}', '{} Consume Access for token groups: '{}'",
        webhookApi.title,
        webhookApi.consumerAccess,
        webhookApi.consumerIAMGroups,
        tokenGroups
      )
    }

    return consumerAccessVoters
      .any {
        it.vote(webhookApi, tokenGroups)
      }
  }

  fun webhookApiIsWritableFor(webhookApi: WebhookApi, tokenGroups: Collection<String>): Boolean {
    if (log.isDebugEnabled) {
      log.debug(
        "Checking WebhookApi '{}', '{}', '{} Provide Access for token groups: '{}'",
        webhookApi.title,
        webhookApi.providerAccess,
        webhookApi.providerIAMGroups,
        tokenGroups
      )
    }

    return providerAccessVoters
      .any {
        it.vote(webhookApi, tokenGroups)
      }
  }
}
