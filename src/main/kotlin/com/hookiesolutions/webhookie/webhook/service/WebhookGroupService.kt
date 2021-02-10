package com.hookiesolutions.webhookie.webhook.service

import com.hookiesolutions.webhookie.common.Constants.Channels.Admin.Companion.ADMIN_CONSUMER_GROUP_DELETED_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Admin.Companion.ADMIN_CONSUMER_GROUP_UPDATED_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Admin.Companion.ADMIN_PROVIDER_GROUP_DELETED_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Admin.Companion.ADMIN_PROVIDER_GROUP_UPDATED_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_PROVIDER
import com.hookiesolutions.webhookie.common.message.entity.EntityDeletedMessage
import com.hookiesolutions.webhookie.common.message.entity.EntityUpdatedMessage
import com.hookiesolutions.webhookie.common.model.DeletableEntity.Companion.deletable
import com.hookiesolutions.webhookie.common.model.UpdatableEntity.Companion.updatable
import com.hookiesolutions.webhookie.common.service.AdminServiceDelegate
import com.hookiesolutions.webhookie.webhook.domain.Topic
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_CONSUMER_IAM_GROUPS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup.Keys.Companion.KEY_PROVIDER_IAM_GROUPS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroupRepository
import com.hookiesolutions.webhookie.webhook.service.model.WebhookGroupRequest
import com.hookiesolutions.webhookie.webhook.service.security.WebhookSecurityService
import org.slf4j.Logger
import org.springframework.data.domain.Pageable
import org.springframework.integration.annotation.ServiceActivator
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
class WebhookGroupService(
  private val repository: WebhookGroupRepository,
  private val securityService: WebhookSecurityService,
  private val adminServiceDelegate: AdminServiceDelegate,
  private val log: Logger,
) {
  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun createWebhookGroup(request: WebhookGroupRequest): Mono<WebhookGroup> {
    return verifyRequestGroups(request)
      .map { request.toWebhookGroup() }
      .flatMap {
        log.info("Saving WebhookGroup: '{}'", it.title)
        repository.save(it)
      }
  }

  @PreAuthorize("permitAll()")
  fun findMyWebhookGroups(pageable: Pageable): Flux<WebhookGroup> {
    return securityService.tokenGroups()
      .switchIfEmpty(emptyList<String>().toMono())
      .flatMapMany {
        repository.findMyWebhookGroups(it, pageable)
      }
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun readWebhookGroup(id: String): Mono<WebhookGroup> {
    return repository.findByIdVerifyingReadAccess(id)
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun deleteWebhookGroup(id: String): Mono<String> {
    return repository.findByIdVerifyingWriteAccess(id)
      .map { deletable(it) }
      .flatMap { repository.delete(it) }
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun updateWebhookGroup(id: String, request: WebhookGroupRequest): Mono<WebhookGroup> {
    return repository.findByIdVerifyingWriteAccess(id)
      .flatMap { verifyRequestGroups(request) }
      .map { updatable(request.toWebhookGroup()) }
      .flatMap { repository.update(it, id) }
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun myTopics(): Flux<Topic> {
    return securityService.tokenGroups()
      .doOnNext { log.info("Fetching all topics for groups: '{}'", it) }
      .flatMapMany { repository.myTopicsAsProvider(it) }
  }

  @ServiceActivator(inputChannel = ADMIN_CONSUMER_GROUP_DELETED_CHANNEL_NAME)
  fun removeConsumerGroup(message: EntityDeletedMessage<String>) {
    removeAccessGroup(message, KEY_CONSUMER_IAM_GROUPS)
  }

  @ServiceActivator(inputChannel = ADMIN_PROVIDER_GROUP_DELETED_CHANNEL_NAME)
  fun removeProviderGroup(message: EntityDeletedMessage<String>) {
    removeAccessGroup(message, KEY_PROVIDER_IAM_GROUPS)
  }

  fun removeAccessGroup(message: EntityDeletedMessage<String>, attrName: String) {
    repository.removeAccessGroup(message.value, attrName)
      .subscribe {
        log.info("All '{}' {}s removed", message.value, message.type)
      }
  }

  @ServiceActivator(inputChannel = ADMIN_CONSUMER_GROUP_UPDATED_CHANNEL_NAME)
  fun updateConsumerGroup(message: EntityUpdatedMessage<String>) {
    updateAccessGroup(message, KEY_CONSUMER_IAM_GROUPS)
  }

  @ServiceActivator(inputChannel = ADMIN_PROVIDER_GROUP_UPDATED_CHANNEL_NAME)
  fun updateProviderGroup(message: EntityUpdatedMessage<String>) {
    updateAccessGroup(message, KEY_PROVIDER_IAM_GROUPS)
  }

  fun updateAccessGroup(message: EntityUpdatedMessage<String>, attrName: String) {
    repository.updateAccessGroup(message.oldValue, message.newValue, attrName)
      .subscribe {
        log.info("All '{}' {}s updated to '{}'", message.oldValue, message.type, message.newValue)
      }
  }

  private fun verifyRequestGroups(request: WebhookGroupRequest): Mono<Boolean> {
    return adminServiceDelegate.verifyGroups(request.consumerGroups, request.providerGroups)
  }
}