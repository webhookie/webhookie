package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_CONSUMER
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_PROVIDER
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.DeletableEntity.Companion.deletable
import com.hookiesolutions.webhookie.common.model.RoleActor
import com.hookiesolutions.webhookie.common.model.UpdatableEntity.Companion.updatable
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.common.service.IdGenerator
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.subscription.domain.ApplicationRepository
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.CallbackRepository
import com.hookiesolutions.webhookie.subscription.domain.StatusUpdate.Companion.activated
import com.hookiesolutions.webhookie.subscription.domain.StatusUpdate.Companion.deactivated
import com.hookiesolutions.webhookie.subscription.domain.StatusUpdate.Companion.suspended
import com.hookiesolutions.webhookie.subscription.domain.StatusUpdate.Companion.validated
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionRepository
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionStatus
import com.hookiesolutions.webhookie.subscription.service.factory.ConversionsFactory
import com.hookiesolutions.webhookie.subscription.service.model.CreateSubscriptionRequest
import com.hookiesolutions.webhookie.subscription.service.model.UpdateSubscriptionRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.ReasonRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.ValidateSubscriptionRequest
import com.hookiesolutions.webhookie.subscription.service.validator.SubscriptionValidator
import com.hookiesolutions.webhookie.webhook.service.WebhookGroupServiceDelegate
import org.slf4j.Logger
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/12/20 15:09
 */
@Service
class SubscriptionService(
  private val log: Logger,
  private val securityHandler: SecurityHandler,
  private val timeMachine: TimeMachine,
  private val signor: SubscriptionSignor,
  private val idGenerator: IdGenerator,
  private val factory: ConversionsFactory,
  private val repository: SubscriptionRepository,
  private val callbackRepository: CallbackRepository,
  private val applicationRepository: ApplicationRepository,
  private val stateManager: SubscriptionStateManager,
  private val subscriptionValidator: SubscriptionValidator,
  private val webhookServiceDelegate: WebhookGroupServiceDelegate
) {
  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun createSubscription(request: CreateSubscriptionRequest): Mono<Subscription> {
    log.info("Subscribing to '{}' using callback: '{}'", request.topic, request.callbackId)
    return callbackRepository
      .findById(request.callbackId)
      .zipWhen { applicationRepository.findByIdVerifyingWriteAccess(it.applicationId) }
      .map { factory.createSubscription(it.t2, it.t1, request)}
      .flatMap { repository.save(it) }
      .doOnNext { log.info("Subscribed to '{}' using callback: '{}'", it.topic, it.callback.requestTarget()) }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun subscriptionById(id: String): Mono<Subscription> {
    log.info("Fetching Subscription by id: '{}'", id)
    return repository.findByIdVerifyingReadAccess(id)
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun deleteSubscription(id: String): Mono<String> {
    log.info("Deleting Subscription by id: '{}'", id)
    return repository.findByIdVerifyingWriteAccess(id)
      .map { deletable(it) }
      .flatMap { repository.delete(it) }
  }

  @PreAuthorize("isAuthenticated()")
  fun subscriptions(role: RoleActor): Flux<Subscription> {
    return when (role) {
      RoleActor.CONSUMER -> {
        consumerSubscriptions()
      }
      RoleActor.PROVIDER -> {
        providerSubscriptions()
      }
    }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun consumerSubscriptions(): Flux<Subscription> {
    return securityHandler.data()
      .doOnNext { log.info("Fetching all subscriptions for token: '{}'", it) }
      .flatMapMany { repository.findAllConsumerSubscriptions(it.entity, it.groups) }
  }

  @PreAuthorize("hasAuthority('$ROLE_PROVIDER')")
  fun providerSubscriptions(): Flux<Subscription> {
    log.info("Fetching provider topics...")
    return webhookServiceDelegate.providerTopics()
      .doOnNext { log.info("Fetching topic subscriptions for topics: '{}'", it) }
      .flatMapMany { repository.topicSubscriptions(it) }
  }

  @Suppress("unused")
  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun updateSubscription(id: String, request: UpdateSubscriptionRequest): Mono<Subscription> {
    log.info("Updating Subscription '{}' using callback: '{}'", id, request.callbackId)
    return repository
      .findByIdVerifyingWriteAccess(id)
      .flatMap { subscription ->
        return@flatMap if(subscription.callback.callbackId == request.callbackId) {
          subscription.toMono()
        } else {
          callbackRepository
            .findById(request.callbackId)
            .zipWhen { applicationRepository.findById(it.applicationId) }
            .map { factory.modifySubscription(it.t2, it.t1, subscription, request)}
            .map { updatable(it) }
            .flatMap { repository.update(it, id) }
            .doOnNext { log.info("Subscription '{}' was modified to callback: '{}'", it.id, it.callback.requestTarget()) }
        }
      }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun validateSubscription(id: String, request: ValidateSubscriptionRequest): Mono<SubscriptionStatus> {
    log.info("Validating Subscription '{}'...", id)

    return repository
      .findByIdVerifyingWriteAccess(id)
      .zipWhen { stateManager.canBeValidated(it) }
      .zipWhen { subscriptionValidator.validate(it.t1, request) }
      .flatMap { repository.statusUpdate(id, validated(timeMachine.now()), it.t1.t2) }
      .doOnNext { log.info("Subscription '{}' validated successfully", id) }
      .map { it.statusUpdate.status }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun activateSubscription(id: String): Mono<SubscriptionStatus> {
    log.info("Activating Subscription '{}'...", id)

    return repository
      .findByIdVerifyingWriteAccess(id)
      .zipWhen { stateManager.canBeActivated(it) }
      .flatMap { repository.statusUpdate(id, activated(timeMachine.now()), it.t2) }
      .doOnNext { log.info("Subscription '{}' activated successfully", id) }
      .map { it.statusUpdate.status }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun deactivateSubscription(id: String, request: ReasonRequest): Mono<SubscriptionStatus> {
    log.info("Deactivating Subscription '{}'...", id)

    return repository
      .findByIdVerifyingWriteAccess(id)
      .zipWhen { stateManager.canBeDeactivated(it) }
      .flatMap { repository.statusUpdate(id, deactivated(timeMachine.now(), request.reason), it.t2) }
      .doOnNext { log.info("Subscription '{}' deactivated successfully", id) }
      .map { it.statusUpdate.status }
  }

  @PreAuthorize("hasAuthority('$ROLE_PROVIDER')")
  fun suspendSubscription(id: String, request: ReasonRequest): Mono<SubscriptionStatus> {
    log.info("Suspending Subscription '{}'...", id)

    return repository
      .findByIdVerifyingProviderAccess(id)
      .zipWhen { stateManager.canBeSuspended(it) }
      .flatMap { repository.statusUpdate(id, suspended(timeMachine.now(), request.reason), it.t2) }
      .doOnNext { log.info("Subscription '{}' suspended successfully", id) }
      .map { it.statusUpdate.status }
  }

  @PreAuthorize("hasAuthority('$ROLE_PROVIDER')")
  fun unsuspendSubscription(id: String, request: ReasonRequest): Mono<SubscriptionStatus> {
    log.info("Un-suspending Subscription '{}'...", id)

    return repository
      .findByIdVerifyingProviderAccess(id)
      .zipWhen { stateManager.canBeUnsuspended(it) }
      .flatMap { repository.statusUpdate(id, deactivated(timeMachine.now(), request.reason), it.t2) }
      .doOnNext { log.info("Subscription '{}' unsuspended successfully", id) }
      .map { it.statusUpdate.status }
  }

  fun findSubscriptionsFor(consumerMessage: ConsumerMessage): Flux<Subscription> {
    val topic = consumerMessage.headers.topic
    val authorizedSubscribers = consumerMessage.headers.authorizedSubscribers

    log.info("Reading '{}' subscribers for authorized subscribers: {}", topic, authorizedSubscribers)

    return repository
      .findAuthorizedTopicSubscriptions(topic, authorizedSubscribers)
  }

  fun saveBlockedSubscriptionMessage(message: BlockedSubscriptionMessage): Mono<BlockedSubscriptionMessage> {
    log.info("Saving BlockedSubscriptionMessage: '{}'", message.subscription.callback.url)
    return repository
      .saveBlockedSubscriptionMessage(message)
  }

  fun blockSubscriptionFor(message: PublisherErrorMessage): Mono<BlockedDetailsDTO> {
    val subscription = message.subscriptionMessage.subscription
    val time = timeMachine.now()
    val details = BlockedDetailsDTO(message.reason, time)

    log.info(
      "updating subscription: '{}' as and the reason is: '{}'",
      subscription.callback.url,
      details.reason
    )

    return repository
      .blockSubscriptionWithReason(subscription.id, details)
      .doOnNext {
        log.info("Subscription({}) was blocked because '{}'", subscription.id, details.reason)
      }
      .map { details }
  }

  fun findAllBlockedMessagesForSubscription(id: String): Flux<BlockedSubscriptionMessage> {
    log.info("Fetching all blocked messages for subscription: '{}'", id)
    return repository
      .findAllBlockedMessagesForSubscription(id)
  }

  fun unblockSubscriptionBy(id: String): Mono<Subscription> {
    return repository
      .unblockSubscription(id)
  }

  fun deleteBlockedMessage(bsm: BlockedSubscriptionMessage): Mono<BlockedSubscriptionMessage> {
    return repository
      .deleteBlockedSubscriptionMessage(bsm)
      .map { bsm }
  }

  fun signSubscriptionMessage(subscriptionMessage: SignableSubscriptionMessage): Mono<SignableSubscriptionMessage> {
    return repository
      .findById(subscriptionMessage.subscription.id)
      .flatMap { signor.sign(it, subscriptionMessage.originalMessage, idGenerator.generate()) }
      .map { factory.createSignedSubscriptionMessage(subscriptionMessage, it) }
  }

  fun enrichBlockedSubscriptionMessageReloadingSubscription(message: BlockedSubscriptionMessage): Mono<BlockedSubscriptionMessage> {
    return repository
      .findById(message.subscription.id)
      .map { factory.updateBlockedSubscriptionMessageWithSubscription(message, it) }
  }
}