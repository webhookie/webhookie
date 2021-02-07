package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_CONSUMER
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.DeletableEntity.Companion.deletable
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.common.service.IdGenerator
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.domain.ApplicationRepository
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.CallbackRepository
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionRepository
import com.hookiesolutions.webhookie.subscription.service.model.CreateSubscriptionRequest
import org.slf4j.Logger
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 4/12/20 15:09
 */
@Service
class SubscriptionService(
  private val log: Logger,
  private val timeMachine: TimeMachine,
  private val signor: SubscriptionSignor,
  private val idGenerator: IdGenerator,
  private val factory: ConversionsFactory,
  private val repository: SubscriptionRepository,
  private val callbackRepository: CallbackRepository,
  private val applicationRepository: ApplicationRepository
) {
  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  fun createSubscription(request: CreateSubscriptionRequest): Mono<Subscription> {
    log.info("Subscribing to '{}' using callback: '{}'", request.topic, request.callbackId)
    return callbackRepository
      .findById(request.callbackId)
      .zipWhen { applicationRepository.findByIdVerifyingReadAccess(it.applicationId) }
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