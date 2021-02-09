package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.Constants.Channels.Admin.Companion.ADMIN_CONSUMER_GROUP_DELETED_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Channels.Admin.Companion.ADMIN_CONSUMER_GROUP_UPDATED_CHANNEL_NAME
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_CONSUMER
import com.hookiesolutions.webhookie.common.message.entity.EntityDeletedMessage
import com.hookiesolutions.webhookie.common.message.entity.EntityUpdatedMessage
import com.hookiesolutions.webhookie.common.model.DeletableEntity.Companion.deletable
import com.hookiesolutions.webhookie.common.model.UpdatableEntity.Companion.updatable
import com.hookiesolutions.webhookie.common.service.AdminServiceDelegate
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.ApplicationRepository
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionRepository
import com.hookiesolutions.webhookie.subscription.service.model.ApplicationRequest
import org.slf4j.Logger
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 22:42
 */
@Service
class ApplicationService(
  private val log: Logger,
  private val factory: ConversionsFactory,
  private val adminServiceDelegate: AdminServiceDelegate,
  private val securityHandler: SecurityHandler,
  private val subscriptionRepository: SubscriptionRepository,
  private val repository: ApplicationRepository,
) {
  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun createApplication(body: ApplicationRequest): Mono<Application> {
    return adminServiceDelegate.extractMyValidConsumerGroups(body.consumerGroups)
      .zipWith(securityHandler.entity())
      .map { factory.createApplicationRequestToApplication(body, it.t2, it.t1) }
      .flatMap { repository.save(it) }
      .doOnNext { log.info("Application '{}' was created successfully", it.name) }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun userApplications(): Flux<Application> {
    return securityHandler.data()
      .doOnNext { log.info("Fetching all applications for entity: '{}'", it) }
      .flatMapMany { repository.userApplications(it.entity, it.groups) }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun applicationById(id: String): Mono<Application> {
    log.info("Fetching Application by id: '{}'", id)
    return repository.findByIdVerifyingReadAccess(id)
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun deleteApplication(id: String): Mono<String> {
    return repository.findByIdVerifyingWriteAccess(id)
      .map { deletable(it) }
      .flatMap { repository.delete(it) }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun updateApplication(id: String, request: ApplicationRequest): Mono<Application> {
    return adminServiceDelegate.extractMyValidConsumerGroups(request.consumerGroups)
      .zipWhen { repository.findByIdVerifyingWriteAccess(id) }
      .map { it.t2.copy(name = request.name, description = request.description, consumerIAMGroups = it.t1) }
      .map { updatable(it) }
      .flatMap { repository.update(it, id) }
      .zipWhen {
        log.info("Updating all Application '{}' Subscriptions", it.name)
        subscriptionRepository.updateApplicationSubscriptions(id, it.details())
      }
      .doOnNext { log.info("Application '{}' was updated successfully", it.t1.name) }
      .map { it.t1 }
  }

  @ServiceActivator(inputChannel = ADMIN_CONSUMER_GROUP_DELETED_CHANNEL_NAME)
  fun removeAccessGroup(message: EntityDeletedMessage<String>) {
    repository.removeConsumerGroup(message.value)
      .subscribe {
        log.info("All '{}' ConsumerGroups removed", message.value)
      }
  }

  @ServiceActivator(inputChannel = ADMIN_CONSUMER_GROUP_UPDATED_CHANNEL_NAME)
  fun updateAccessGroup(message: EntityUpdatedMessage<String>) {
    repository.updateConsumerGroup(message.oldValue, message.newValue)
      .subscribe {
        log.info("All '{}' ConsumerGroups updated to '{}'", message.oldValue, message.newValue)
      }
  }
}