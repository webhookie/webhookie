package com.hookiesolutions.webhookie.webhook.service

import com.hookiesolutions.webhookie.common.Constants.Channels.Admin.Companion.ADMIN_CONSUMER_GROUP_DELETED_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Admin.Companion.ADMIN_CONSUMER_GROUP_UPDATED_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Admin.Companion.ADMIN_PROVIDER_GROUP_DELETED_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Admin.Companion.ADMIN_PROVIDER_GROUP_UPDATED_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.SUBSCRIPTION_ACTIVATED_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Subscription.Companion.SUBSCRIPTION_DEACTIVATED_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_PROVIDER
import com.hookiesolutions.webhookie.common.message.entity.EntityDeletedMessage
import com.hookiesolutions.webhookie.common.message.entity.EntityUpdatedMessage
import com.hookiesolutions.webhookie.common.model.DeletableEntity.Companion.deletable
import com.hookiesolutions.webhookie.common.model.UpdatableEntity.Companion.updatable
import com.hookiesolutions.webhookie.common.service.AccessGroupServiceDelegate
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
import org.springframework.messaging.Message
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
  private val accessGroupServiceDelegate: AccessGroupServiceDelegate,
  private val asyncApiService: AsyncApiService,
  private val log: Logger,
) {
  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun createWebhookGroup(request: WebhookGroupRequest): Mono<WebhookGroup> {
    return verifyRequestGroups(request)
      .flatMap { asyncApiService.parseAsyncApiSpecToWebhookApi(request) }
      .map { WebhookGroup.create(it, request) }
      .flatMap {
        log.info("Saving WebhookGroup: '{}'", it.title)
        repository.save(it)
      }
  }

  @PreAuthorize("permitAll()")
  fun findMyWebhookGroups(pageable: Pageable): Flux<WebhookGroup> {
    return securityService.tokenGroups()
      .switchIfEmpty(emptyList<String>().toMono())
      .doOnNext { log.info("Fetching all webhook groups for auth: '{}'", it) }
      .flatMapMany {
        repository.findMyWebhookGroups(it, pageable)
      }
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun readWebhookGroup(id: String): Mono<WebhookGroup> {
    return repository.findByIdVerifyingReadAccess(id)
  }

  @PreAuthorize("permitAll()")
  fun readWebhookGroupByTopic(topic: String): Mono<WebhookGroup> {
    return repository.findByTopicVerifyingReadAccess(topic)
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun deleteWebhookGroup(id: String): Mono<String> {
    return repository.findByIdVerifyingWriteAccess(id)
      .map { deletable(it) }
      .flatMap { repository.delete(it) }
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun updateWebhookGroup(id: String, request: WebhookGroupRequest): Mono<WebhookGroup> {
    return verifyRequestGroups(request)
      .flatMap { asyncApiService.parseAsyncApiSpecToWebhookApi(request) }
      .flatMap { spec ->
        repository.findByIdVerifyingWriteAccess(id)
          .map { WebhookGroup.create(spec, request, it) }
      }
      .map { updatable(it) }
      .flatMap { repository.update(it, id) }
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun myTopics(): Flux<Topic> {
    return securityService.tokenGroups()
      .doOnNext { log.info("Fetching all webhooks for groups: '{}'", it) }
      .flatMapMany { repository.myTopicsAsProvider(it) }
  }

  @ServiceActivator(inputChannel = ADMIN_CONSUMER_GROUP_DELETED_CHANNEL_NAME)
  fun removeConsumerGroup(@Suppress("SpringJavaInjectionPointsAutowiringInspection") message: EntityDeletedMessage<String>) {
    removeAccessGroup(message, KEY_CONSUMER_IAM_GROUPS)
  }

  @ServiceActivator(inputChannel = ADMIN_PROVIDER_GROUP_DELETED_CHANNEL_NAME)
  fun removeProviderGroup(@Suppress("SpringJavaInjectionPointsAutowiringInspection") message: EntityDeletedMessage<String>) {
    removeAccessGroup(message, KEY_PROVIDER_IAM_GROUPS)
  }

  fun removeAccessGroup(message: EntityDeletedMessage<String>, attrName: String) {
    repository.removeAccessGroup(message.value, attrName)
      .subscribe {
        log.info("All '{}' {}s removed", message.value, message.type)
      }
  }

  @ServiceActivator(inputChannel = ADMIN_CONSUMER_GROUP_UPDATED_CHANNEL_NAME)
  fun updateConsumerGroup(@Suppress("SpringJavaInjectionPointsAutowiringInspection") message: EntityUpdatedMessage<String>) {
    updateAccessGroup(message, KEY_CONSUMER_IAM_GROUPS)
  }

  @ServiceActivator(inputChannel = ADMIN_PROVIDER_GROUP_UPDATED_CHANNEL_NAME)
  fun updateProviderGroup(@Suppress("SpringJavaInjectionPointsAutowiringInspection") message: EntityUpdatedMessage<String>) {
    updateAccessGroup(message, KEY_PROVIDER_IAM_GROUPS)
  }

  fun updateAccessGroup(message: EntityUpdatedMessage<String>, attrName: String) {
    repository.updateAccessGroup(message.oldValue, message.newValue, attrName)
      .subscribe {
        log.info("All '{}' {}s updated to '{}'", message.oldValue, message.type, message.newValue)
      }
  }

  @ServiceActivator(inputChannel = SUBSCRIPTION_ACTIVATED_CHANNEL_NAME)
  fun increaseWebhookSubscribers(@Suppress("SpringJavaInjectionPointsAutowiringInspection") message: Message<String>) {
    repository.incTopicSubscriptions(message.payload, 1)
      .subscribe {
        log.info("Increased number of subscriptions for webhook: '{}'", message.payload)
      }
  }

  @ServiceActivator(inputChannel = SUBSCRIPTION_DEACTIVATED_CHANNEL_NAME)
  fun decreaseWebhookSubscribers(@Suppress("SpringJavaInjectionPointsAutowiringInspection") message: Message<String>) {
    repository.incTopicSubscriptions(message.payload, -1)
      .subscribe {
        log.info("Decreased number of subscriptions for webhook: '{}'", message.payload)
      }
  }

  private fun verifyRequestGroups(request: WebhookGroupRequest): Mono<Boolean> {
    return accessGroupServiceDelegate.verifyGroups(request.consumerGroups, request.providerGroups)
  }
}
