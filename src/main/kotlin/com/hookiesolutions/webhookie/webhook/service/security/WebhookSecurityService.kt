package com.hookiesolutions.webhookie.webhook.service.security

import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup
import com.hookiesolutions.webhookie.webhook.service.security.voter.WebhookGroupConsumeAccessVoter
import org.slf4j.Logger
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
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
  private val log: Logger
) {
  fun groups(): Mono<List<String>> {
    return securityHandler.groups()
  }

  fun webhookGroupIsConsumableFor(webhookGroup: WebhookGroup, tokenGroups: List<String>): Boolean {
    if (log.isDebugEnabled) {
      log.debug(
        "Checking WebhookGroup '{}', '{}', '{} Consume Access for token groups: '{}'",
        webhookGroup.name,
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

  fun verifyConsumeAccess(webhookGroupSupplier: Supplier<Mono<WebhookGroup>>): Mono<WebhookGroup> {
    return groups()
      .zipWith(webhookGroupSupplier.get())
      .flatMap {
        val webhookGroup = it.t2
        val tokenGroups = it.t1
        return@flatMap if (webhookGroupIsConsumableFor(webhookGroup, tokenGroups)) {
          webhookGroup.toMono()
        } else {
          if (log.isDebugEnabled) {
            log.debug("Access is denied for current user to read WebhookGroup: '{}'", webhookGroup.name)
          }
          Mono.error(AccessDeniedException("Access Denied!"))
        }
      }
  }
}