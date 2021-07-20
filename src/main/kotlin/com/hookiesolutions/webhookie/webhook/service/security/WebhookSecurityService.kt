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
