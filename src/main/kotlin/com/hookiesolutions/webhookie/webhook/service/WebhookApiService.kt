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
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Keys.Companion.KEY_CONSUMER_IAM_GROUPS
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi.Keys.Companion.KEY_PROVIDER_IAM_GROUPS
import com.hookiesolutions.webhookie.webhook.domain.WebhookApiRepository
import com.hookiesolutions.webhookie.webhook.service.model.WebhookApiRequest
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
class WebhookApiService(
  private val repository: WebhookApiRepository,
  private val securityService: WebhookSecurityService,
  private val accessGroupServiceDelegate: AccessGroupServiceDelegate,
  private val asyncApiService: AsyncApiService,
  private val log: Logger,
) {
  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun createWebhookApi(request: WebhookApiRequest): Mono<WebhookApi> {
    return verifyRequestGroups(request)
      .flatMap { asyncApiService.parseAsyncApiSpecToWebhookApi(request) }
      .map { WebhookApi.create(it, request) }
      .flatMap {
        log.info("Saving WebhookApi: '{}'", it.title)
        repository.save(it)
      }
  }

  @PreAuthorize("permitAll()")
  fun findMyWebhookApis(pageable: Pageable): Flux<WebhookApi> {
    return securityService.tokenGroups()
      .switchIfEmpty(emptyList<String>().toMono())
      .doOnNext { log.info("Fetching all webhook apis for auth: '{}'", it) }
      .flatMapMany {
        repository.findMyWebhookApis(it, pageable)
      }
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun readWebhookApi(id: String): Mono<WebhookApi> {
    return repository.findByIdVerifyingReadAccess(id)
  }

  @PreAuthorize("permitAll()")
  fun readWebhookApiByTopic(topic: String): Mono<WebhookApi> {
    return repository.findByTopicVerifyingReadAccess(topic)
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun deleteWebhookApi(id: String): Mono<String> {
    return repository.findByIdVerifyingWriteAccess(id)
      .map { deletable(it) }
      .flatMap { repository.delete(it) }
  }

  @PreAuthorize("hasAuthority('${ROLE_PROVIDER}')")
  fun updateWebhookApi(id: String, request: WebhookApiRequest): Mono<WebhookApi> {
    return verifyRequestGroups(request)
      .flatMap { asyncApiService.parseAsyncApiSpecToWebhookApi(request) }
      .flatMap { spec ->
        repository.findByIdVerifyingWriteAccess(id)
          .map { WebhookApi.create(spec, request, it) }
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

  private fun verifyRequestGroups(request: WebhookApiRequest): Mono<Boolean> {
    return accessGroupServiceDelegate.verifyGroups(request.consumerGroups, request.providerGroups)
  }
}
