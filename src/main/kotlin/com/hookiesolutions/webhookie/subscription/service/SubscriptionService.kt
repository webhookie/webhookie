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

package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.Constants.Channels.Webhook.Companion.WEBHOOK_API_DELETED_CHANNEL
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_ADMIN
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_CONSUMER
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_PROVIDER
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.GenericSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.RetryableSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.DeletableEntity.Companion.deletable
import com.hookiesolutions.webhookie.common.model.RoleActor
import com.hookiesolutions.webhookie.common.model.StatusCountRow
import com.hookiesolutions.webhookie.common.model.TimedResult
import com.hookiesolutions.webhookie.common.model.UpdatableEntity.Companion.updatable
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate.Companion.activated
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate.Companion.approved
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate.Companion.blocked
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate.Companion.deactivated
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate.Companion.rejected
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate.Companion.submitted
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate.Companion.suspended
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate.Companion.updateStatus
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionStatus
import com.hookiesolutions.webhookie.common.repository.GenericRepository.Companion.fieldName
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import com.hookiesolutions.webhookie.subscription.domain.ApplicationRepository
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessageRepository
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionApprovalDetails
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionApprovalResult
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionRepository
import com.hookiesolutions.webhookie.subscription.domain.callback.CallbackRepository
import com.hookiesolutions.webhookie.subscription.service.factory.ConversionsFactory
import com.hookiesolutions.webhookie.subscription.service.model.subscription.ApproveSubscriptionRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.CreateSubscriptionRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.ReasonRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.RejectSubscriptionRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.SubscriptionApprovalRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.UpdateSubscriptionRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.VerifySubscriptionRequest
import com.hookiesolutions.webhookie.subscription.service.state.SubscriptionStateManager
import com.hookiesolutions.webhookie.subscription.service.verifier.SubscriptionVerifier
import com.hookiesolutions.webhookie.webhook.service.WebhookApiServiceDelegate
import org.slf4j.Logger
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuples
import java.time.Instant


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
  private val factory: ConversionsFactory,
  private val repository: SubscriptionRepository,
  private val bsmRepository: BlockedSubscriptionMessageRepository,
  private val callbackRepository: CallbackRepository,
  private val applicationRepository: ApplicationRepository,
  private val stateManager: SubscriptionStateManager,
  private val subscriptionVerifier: SubscriptionVerifier,
  private val unblockedSubscriptionChannel: MessageChannel,
  private val subscriptionActivatedChannel: MessageChannel,
  private val subscriptionDeactivatedChannel: MessageChannel,
  private val webhookServiceDelegate: WebhookApiServiceDelegate
) {
  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun createSubscription(request: CreateSubscriptionRequest): Mono<Subscription> {
    log.info("Subscribing to '{}' using callback: '{}'", request.topic, request.callbackId)
    return callbackRepository
      .findById(request.callbackId)
      .zipWhen { applicationRepository.findByIdVerifyingReadAccess(it.applicationId) }
      .map { factory.createSubscription(it.t2, it.t1, request)}
      .flatMap {
        repository.findDraftSubscription(it.topic, it.callback.callbackId)
          .switchIfEmpty { repository.save(it) }
      }
      .doOnNext { log.info("Subscribed to '{}' using callback: '{}'", it.topic, it.callback.requestTarget()) }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun subscriptionByIdVerifyingReadAccess(id: String): Mono<Subscription> {
    log.info("Fetching Subscription by id Verifying Read Access: '{}'", id)
    return repository.findByIdVerifyingReadAccess(id)
  }

  fun subscriptionById(id: String): Mono<Subscription> {
    return repository.findById(id)
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun deleteSubscription(id: String): Mono<String> {
    log.info("Deleting Subscription by id: '{}'", id)
    return repository.findByIdVerifyingWriteAccess(id)
      .flatMap { stateManager.canBeDeleted(it) }
      .map { deletable(it) }
      .flatMap { repository.delete(it) }
      .flatMap { bsmRepository.deleteSubscriptionMessages(id) }
      .map { it.toString() }
  }

  @PreAuthorize("isAuthenticated()")
  fun subscriptions(
    role: RoleActor,
    pageable: Pageable,
    topic: String?,
    callbackId: String?
  ): Flux<Subscription> {
    return when (role) {
      RoleActor.CONSUMER -> {
        consumerSubscriptions(pageable, topic, callbackId)
      }
      RoleActor.PROVIDER -> {
        providerSubscriptions(topic, pageable)
      }
    }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun consumerSubscriptions(
    pageable: Pageable,
    topic: String?,
    callbackId: String?
  ): Flux<Subscription> {
    return securityHandler.data()
      .doOnNext { log.info("Fetching all subscriptions for token: '{}'", it) }
      .flatMapMany { repository.findAllConsumerSubscriptions(it.entity, it.email, pageable, topic, callbackId) }
  }

  @PreAuthorize("hasAnyAuthority('$ROLE_PROVIDER', '$ROLE_ADMIN')")
  fun providerSubscriptions(
    topic: String?,
    pageable: Pageable
  ): Flux<Subscription> {
    log.info("Fetching provider topics...")
    return webhookServiceDelegate.providerTopicsConsideringAdmin()
      .doOnNext { log.info("Fetching topic subscriptions for topics: '{}', isAdmin: '{}'", it.topics, it.isAdmin) }
      .flatMapMany { repository.topicSubscriptions(topic, it.topics, it.isAdmin, pageable) }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun updateSubscription(id: String, request: UpdateSubscriptionRequest): Mono<Subscription> {
    log.info("Updating Subscription '{}' using callback: '{}'", id, request.callbackId)
    return repository
      .findByIdVerifyingWriteAccess(id)
      .flatMap { stateManager.canBeUpdated(it) }
      .flatMap { subscription ->
        return@flatMap if(subscription.callback.callbackId == request.callbackId) {
          subscription.toMono()
        } else {
          callbackRepository
            .findById(request.callbackId)
            .zipWhen { applicationRepository.findByIdVerifyingReadAccess(it.applicationId) }
            .map { factory.modifySubscription(it.t2, it.t1, subscription, request, timeMachine.now())}
            .map { updatable(it) }
            .flatMap { repository.update(it, id) }
            .doOnNext { log.info("Subscription '{}' was modified to callback: '{}'", it.id, it.callback.requestTarget()) }
        }
      }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun verifySubscription(id: String, request: VerifySubscriptionRequest): Mono<ResponseEntity<ByteArray>> {
    log.info("Verifying Subscription '{}'...", id)

    return repository
      .findByIdVerifyingWriteAccess(id)
      .flatMap { stateManager.canBeVerified(it) }
      .zipWhen { subscriptionVerifier.verify(it.subscription, request) }
      .flatMap {
        repository.statusUpdate(id, updateStatus(timeMachine.now(), it.t1.toBe), it.t1.validStatusList)
          .map { _ -> it.t2 }
      }
      .doOnNext { log.info("Subscription '{}' validated successfully", id) }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun activateSubscription(id: String): Mono<SubscriptionStatus> {
    log.info("Activating Subscription '{}'...", id)

    return repository
      .findByIdVerifyingWriteAccess(id)
      .zipWhen { stateManager.canBeActivated(it) }
      .flatMap { repository.statusUpdate(id, activated(timeMachine.now()), it.t2.validStatusList) }
      .flatMap { s -> callbackRepository.lockCallback(s.callback.callbackId).map { s } }
      .doOnNext { log.info("Subscription '{}' activated successfully", id) }
      .doOnNext {
        log.info("Increasing no of subscriptions for '{}' ...", it.id, it.topic)
        subscriptionActivatedChannel.send(MessageBuilder.withPayload(it.topic).build())
      }
      .map { it.statusUpdate.status }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun submitSubscriptionForApproval(id: String, approvalRequest: SubscriptionApprovalRequest): Mono<Subscription> {
    log.info("Submitting Subscription '{}' for approval", id)

    return repository.findByIdVerifyingWriteAccess(id)
      .zipWhen { stateManager.canBeSubmitted(it) }
      .zipWhen { securityHandler.data() }
      .flatMap {
        val at = timeMachine.now()
        val details = approvalRequest.toSubscriptionApprovalDetails(at, it.t2.email)
        val approvalDetailsPair = Pair(Subscription.Keys.KEY_APPROVAL_DETAILS, details)
        val validStatusList = it.t1.t2.validStatusList
        repository.statusUpdate(id, submitted(at), validStatusList, approvalDetailsPair)
      }
      .flatMap { s -> callbackRepository.lockCallback(s.callback.callbackId).map { s } }
      .doOnNext { log.info("Subscription '{}' submitted for approval successfully", id) }
  }

  @PreAuthorize("hasAuthority('$ROLE_PROVIDER')")
  fun approveSubscription(id: String, request: ApproveSubscriptionRequest): Mono<Subscription> {
    log.info("Approving Submitted Subscription '{}'", id)

    return repository.findByIdVerifyingProviderAccess(id)
      .zipWhen { stateManager.canBeApproved(it) }
      .zipWhen { securityHandler.data() }
      .flatMap {
        val at = timeMachine.now()
        val validStatusList = it.t1.t2.validStatusList
        val profile = request.user.toUserProfile(it.t2.email)
        val approvalResult = SubscriptionApprovalResult(profile, at, SubscriptionStatus.APPROVED)
        val fieldName = fieldName(
          Subscription.Keys.KEY_APPROVAL_DETAILS,
          SubscriptionApprovalDetails.Keys.KEY_APPROVAL_RESULT
        )
        val approvalDetails = Pair(fieldName, approvalResult)
        repository.statusUpdate(id, approved(at), validStatusList, approvalDetails)
      }
      .doOnNext { log.info("Subscription '{}' approved successfully", id) }
  }

  @PreAuthorize("hasAuthority('$ROLE_PROVIDER')")
  fun rejectSubscription(id: String, request: RejectSubscriptionRequest): Mono<Subscription> {
    log.info("Rejecting Submitted Subscription '{}'", id)

    return repository.findByIdVerifyingProviderAccess(id)
      .zipWhen { stateManager.canBeRejected(it) }
      .zipWhen { securityHandler.data() }
      .flatMap {
        val at = timeMachine.now()
        val validStatusList = it.t1.t2.validStatusList
        val profile = request.user.toUserProfile(it.t2.email)
        val approvalResult = SubscriptionApprovalResult(profile, at, SubscriptionStatus.REJECTED, request.reason)
        val fieldName = fieldName(
          Subscription.Keys.KEY_APPROVAL_DETAILS,
          SubscriptionApprovalDetails.Keys.KEY_APPROVAL_RESULT
        )
        val approvalDetails = Pair(fieldName, approvalResult)
        repository.statusUpdate(id, rejected(at), validStatusList, approvalDetails)
      }
      .doOnNext { log.info("Subscription '{}' rejected successfully", id) }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun deactivateSubscription(id: String, request: ReasonRequest): Mono<SubscriptionStatus> {
    log.info("Deactivating Subscription '{}'...", id)

    return repository
      .findByIdVerifyingWriteAccess(id)
      .zipWhen { stateManager.canBeDeactivated(it) }
      .flatMap { repository.statusUpdate(id, deactivated(timeMachine.now(), request.reason), it.t2.validStatusList) }
      .doOnNext { log.info("Subscription '{}' deactivated successfully", id) }
      .doOnNext {
        log.info("Decreasing no of subscriptions for '{}' ...", it.id, it.topic)
        subscriptionDeactivatedChannel.send(MessageBuilder.withPayload(it.topic).build())
      }
      .map { it.statusUpdate.status }
  }

  @PreAuthorize("hasAuthority('$ROLE_PROVIDER')")
  fun suspendSubscription(id: String, request: ReasonRequest): Mono<SubscriptionStatus> {
    log.info("Suspending Subscription '{}'...", id)

    return repository
      .findByIdVerifyingProviderAccess(id)
      .zipWhen { stateManager.canBeSuspended(it) }
      .flatMap { data ->
        repository.statusUpdate(id, suspended(timeMachine.now(), request.reason), data.t2.validStatusList)
          .map { Tuples.of(data.t1, it) }
      }
      .doOnNext { log.info("Subscription '{}' suspended successfully", id) }
      .doOnNext {
        val updatedSubscription = it.t2
        val oldSubscription = it.t1
        if(oldSubscription.statusUpdate.status != SubscriptionStatus.DEACTIVATED) {
          val topic = updatedSubscription.topic
          log.info("Decreasing no of subscriptions for '{}' ...", updatedSubscription.id, topic)
          subscriptionDeactivatedChannel.send(MessageBuilder.withPayload(topic).build())
        }
      }
      .map { it.t2.statusUpdate.status }
  }

  @PreAuthorize("hasAuthority('$ROLE_PROVIDER')")
  fun unsuspendSubscription(id: String, request: ReasonRequest): Mono<SubscriptionStatus> {
    log.info("Un-suspending Subscription '{}'...", id)

    return repository
      .findByIdVerifyingProviderAccess(id)
      .zipWhen { stateManager.canBeUnsuspended(it) }
      .flatMap { repository.statusUpdate(id, deactivated(timeMachine.now(), request.reason), it.t2.validStatusList) }
      .doOnNext { log.info("Subscription '{}' unsuspended successfully", id) }
      .map { it.statusUpdate.status }
  }

  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun unblockSubscription(id: String): Mono<SubscriptionStatus> {
    return repository
      .statusUpdate(id, activated(timeMachine.now()), SubscriptionStatus.values().asList())
      .doOnNext {
        log.info("Subscription '{}' was unblocked! Re-sending blocked messages...", id)
        unblockedSubscriptionChannel.send(MessageBuilder.withPayload(it.dto()).build())
      }
      .map { it.statusUpdate.status }
  }


  @PreAuthorize("hasAnyAuthority('$ROLE_CONSUMER', '$ROLE_PROVIDER')")
  fun readSubmitRequest(id: String): Mono<SubscriptionApprovalDetails> {
    log.info("Getting Subscription '{}' submit request..", id)
    val validStatusList = listOf(
      SubscriptionStatus.SUBMITTED,
      SubscriptionStatus.APPROVED,
      SubscriptionStatus.REJECTED
    )
    return repository.findByIdVerifyingReadAccess(id)
      .filter { validStatusList.contains(it.statusUpdate.status) }
      .switchIfEmpty { EntityNotFoundException("Subscription or the submit request does not exists~").toMono() }
      .map { it.approvalDetails!! }
  }

  fun findSubscriptionsFor(consumerMessage: ConsumerMessage): Flux<Subscription> {
    val topic = consumerMessage.topic
    val authorizedSubscribers = consumerMessage.authorizedSubscribers

    log.info("Reading '{}' subscribers for authorized subscribers: {}", topic, authorizedSubscribers)

    return repository
      .findAuthorizedTopicSubscriptions(topic, authorizedSubscribers)
  }

  fun saveBlockedSubscriptionMessage(message: BlockedSubscriptionMessage): Mono<BlockedSubscriptionMessage> {
    log.info("Saving BlockedSubscriptionMessage: '{}'", message.subscription.callback.url)
    return repository
      .saveBlockedSubscriptionMessage(message)
  }

  fun blockSubscription(id: String, reason: String): Mono<StatusUpdate> {
    val blockedStatusUpdate = blocked(timeMachine.now(), reason)

    log.info(
      "blocking subscription: '{}' with reason is: '{}'",
      id,
      blockedStatusUpdate.reason
    )

    return repository
      .statusUpdate(id, blockedStatusUpdate, SubscriptionStatus.values().asList())
      .doOnNext {
        log.info("Subscription({}) was blocked because '{}'", id, blockedStatusUpdate.reason)
      }
      .map { it.statusUpdate }
  }

  fun blockSubscription(message: PublisherErrorMessage): Mono<StatusUpdate> {
    return blockSubscription(message.subscriptionMessage.subscription.subscriptionId, message.reason)
  }

  fun findAllBlockedMessagesForSubscription(id: String): Flux<BlockedSubscriptionMessage> {
    log.info("Fetching all blocked messages for subscription: '{}'", id)
    return repository
      .findAllBlockedMessagesForSubscription(id)
      .switchIfEmpty {
        log.info("No blocked messages found for subscription: '{}'", id)
      }
  }

  fun deleteBlockedMessage(bsm: BlockedSubscriptionMessage): Mono<BlockedSubscriptionMessage> {
    return repository
      .deleteBlockedSubscriptionMessage(bsm)
      .map { bsm }
  }

  fun signSubscriptionMessage(subscriptionMessage: SignableSubscriptionMessage): Mono<SignableSubscriptionMessage> {
    return repository
      .findById(subscriptionMessage.subscription.subscriptionId)
      .flatMap { signor.sign(it, subscriptionMessage.originalMessage, subscriptionMessage.spanId) }
      .map { factory.createSignedSubscriptionMessage(subscriptionMessage, it) }
  }

  fun enrichSubscriptionMessageReloadingSubscription(message: RetryableSubscriptionMessage): Mono<GenericSubscriptionMessage> {
    return findActiveSubscriptionBy(message.subscriptionId)
      .map { message.updatingSubscriptionCopy(it.dto())}
  }

  private fun findActiveSubscriptionBy(id: String): Mono<Subscription> {
    return repository
      .findById(id)
      .filter { subscriptionCanReceiveMessage(it) }
  }

  private fun subscriptionCanReceiveMessage(subscription: Subscription): Boolean {
    return subscription.statusUpdate.status == SubscriptionStatus.ACTIVATED
  }

  fun subscriptionSummaryBetween(from: Instant, to: Instant): Mono<TimedResult<List<StatusCountRow>>> {
    return repository.timedCountEntitiesGroupByCreatedBetween(
      from,
      to,
      Subscription.Keys.KEY_STATUS_UPDATE,
      StatusUpdate.Keys.KEY_STATUS
    )
  }

  @ServiceActivator(inputChannel = WEBHOOK_API_DELETED_CHANNEL)
  fun suspendTopicSubscriptions(topics: List<String>) {
    log.info("Suspending all subscriptions for '{}' topics due to deleted webhook api", topics.size)
    val statusUpdate = suspended(timeMachine.now(), "Topic has been deleted")
    repository.suspendAllFor(topics, statusUpdate)
      .subscribe {
        log.info("'{}'/'{}' subscription(s) have been suspended due to deleted webhook api", it.t2, it.t1)
      }
  }
}
