package com.hookiesolutions.webhookie.webhook.service

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_PROVIDER
import com.hookiesolutions.webhookie.common.model.DeletableEntity
import com.hookiesolutions.webhookie.common.model.UpdatableEntity
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroupRepository
import com.hookiesolutions.webhookie.webhook.service.model.WebhookGroupRequest
import com.hookiesolutions.webhookie.webhook.service.security.WebhookSecurityService
import org.slf4j.Logger
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 15:31
 */
@Service
class WebhookGroupService(
  private val repository: WebhookGroupRepository,
  private val securityService: WebhookSecurityService,
  private val adminServiceDelegate: AdminServiceDelegate,
  private val log: Logger
) {
  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun createWebhookGroup(request: WebhookGroupRequest): Mono<WebhookGroup> {
    return verifyRequestGroups(request)
      .map { request.toWebhookGroup()}
      .flatMap {
        log.info("Saving WebhookGroup: '{}'", it.title)
        repository.save(it)
      }
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun findProviderWebhookGroups(): Flux<WebhookGroup> {
    return securityService.groups()
      .flatMapMany {
        repository.findProviderWebhookGroups(it)
      }
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun readWebhookGroup(id: String): Mono<WebhookGroup> {
    return repository.findByIdVerifyingReadAccess(id)
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun deleteWebhookGroup(id: String): Mono<String> {
    return repository.findByIdVerifyingWriteAccess(id)
      .map { DeletableEntity(it, true) }
      .flatMap { repository.delete(it) }
      .map { id }
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun updateWebhookGroup(id: String, request: WebhookGroupRequest): Mono<WebhookGroup> {
    return repository.findByIdVerifyingWriteAccess(id)
      .flatMap { verifyRequestGroups(request) }
      .map { UpdatableEntity(request.toWebhookGroup(id), true) }
      .flatMap { repository.update(it) }
  }

  private fun verifyRequestGroups(request: WebhookGroupRequest): Mono<Boolean> {
    return adminServiceDelegate.verifyGroups(request.consumerGroups, request.providerGroups)
  }
}