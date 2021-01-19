package com.hookiesolutions.webhookie.webhook.service

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_PROVIDER
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.portal.service.AccessGroupVerifier
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup
import com.hookiesolutions.webhookie.webhook.domain.WebhookRepository
import com.hookiesolutions.webhookie.webhook.service.model.WebhookGroupRequest
import org.slf4j.Logger
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 15:31
 */
@Service
class WebhookService(
  private val repository: WebhookRepository,
  private val securityHandler: SecurityHandler,
  private val accessGroupVerifier: AccessGroupVerifier,
  private val log: Logger
) {
  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun createGroup(request: WebhookGroupRequest): Mono<WebhookGroup> {
    return request
      .toMono()
      .flatMap { accessGroupVerifier.verifyConsumerGroups(request.consumerGroups) }
      .flatMap { accessGroupVerifier.verifyProviderGroups(request.providerGroups) }
      .map { request.toWebhookGroup()}
      .flatMap {
        log.info("Saving WebhookGroup: '{}'", it.name)
        repository.save(it)
      }
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun findProviderWebhookGroups(): Flux<WebhookGroup> {
    return securityHandler.groups()
      .flatMapMany {
        repository.findProviderWebhookGroups(it)
      }
  }
}