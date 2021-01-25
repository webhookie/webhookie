package com.hookiesolutions.webhookie.webhook.service.security

import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup
import com.hookiesolutions.webhookie.webhook.service.security.voter.WebhookGroupConsumeAccessVoter
import com.hookiesolutions.webhookie.webhook.service.security.voter.WebhookGroupProvideAccessVoter
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
  private val consumerAccessVoters: List<WebhookGroupConsumeAccessVoter>,
  private val providerAccessVoters: List<WebhookGroupProvideAccessVoter>,
  private val log: Logger
) {
  fun tokenGroups(): Mono<List<String>> {
    return securityHandler.tokenGroups()
  }

  fun verifyReadAccess(webhookGroupSupplier: Supplier<Mono<WebhookGroup>>): Mono<WebhookGroup> {
    return verifyAccess(webhookGroupSupplier, this::webhookGroupIsReadableFor)
  }

  fun verifyWriteAccess(webhookGroupSupplier: Supplier<Mono<WebhookGroup>>): Mono<WebhookGroup> {
    return verifyAccess(webhookGroupSupplier, this::webhookGroupIsWritableFor)
  }

  private fun verifyAccess(
    webhookGroupSupplier: Supplier<Mono<WebhookGroup>>,
    verifier: BiFunction<WebhookGroup, Collection<String>, Boolean>
  ): Mono<WebhookGroup> {
    return tokenGroups()
      .zipWith(webhookGroupSupplier.get())
      .flatMap {
        val webhookGroup = it.t2
        val tokenGroups = it.t1
        return@flatMap if (verifier.apply(webhookGroup, tokenGroups)) {
          webhookGroup.toMono()
        } else {
          if (log.isDebugEnabled) {
            log.debug("Access is denied for current user to read WebhookGroup: '{}'", webhookGroup.title)
          }
          Mono.error(AccessDeniedException("Access Denied!"))
        }
      }
  }

  fun webhookGroupIsReadableFor(webhookGroup: WebhookGroup, tokenGroups: Collection<String>): Boolean {
    if (log.isDebugEnabled) {
      log.debug(
        "Checking WebhookGroup '{}', '{}', '{} Consume Access for token groups: '{}'",
        webhookGroup.title,
        webhookGroup.consumerAccess,
        webhookGroup.consumerIAMGroups,
        tokenGroups
      )
    }

    return consumerAccessVoters
      .any {
        it.vote(webhookGroup, tokenGroups)
      }
  }

  fun webhookGroupIsWritableFor(webhookGroup: WebhookGroup, tokenGroups: Collection<String>): Boolean {
    if (log.isDebugEnabled) {
      log.debug(
        "Checking WebhookGroup '{}', '{}', '{} Provide Access for token groups: '{}'",
        webhookGroup.title,
        webhookGroup.providerAccess,
        webhookGroup.providerIAMGroups,
        tokenGroups
      )
    }

    return providerAccessVoters
      .any {
        it.vote(webhookGroup, tokenGroups)
      }
  }
}