package com.hookiesolutions.webhookie.webhook.service.security

import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.webhook.domain.ConsumerAccess
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup
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
  private val log: Logger
) {
  fun groups(): Mono<List<String>> {
    return securityHandler.groups()
  }

  fun webhookGroupIsConsumableFor(webhookGroup: WebhookGroup, tokenGroups: List<String>): Boolean {
    return if (webhookGroup.consumerAccess == ConsumerAccess.PUBLIC) {
      true
    } else {
      tokenGroups.any {
        webhookGroup.consumerIAMGroups.contains(it) ||
        webhookGroup.providerIAMGroups.contains(it)
      }
    }
  }

  fun verifyConsumeAccess(webhookGroupSupplier: Supplier<Mono<WebhookGroup>>): Mono<WebhookGroup> {
    return groups()
      .zipWith(webhookGroupSupplier.get())
      .flatMap {
        val webhookGroup = it.t2
        return@flatMap if(webhookGroupIsConsumableFor(webhookGroup, it.t1)) {
          webhookGroup.toMono()
        } else {
          log.error("Access is denied for current user to view WebhookGroup: '{}'", webhookGroup.name)
          Mono.error(AccessDeniedException("Access Denied!"))
        }
      }
  }
}