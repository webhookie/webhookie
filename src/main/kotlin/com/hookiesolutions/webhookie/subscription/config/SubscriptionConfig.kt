package com.hookiesolutions.webhookie.subscription.config

import com.hookiesolutions.webhookie.common.exception.messaging.SubscriptionMessageHandlingException
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.publisher.GenericPublisherMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.GenericSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.NoSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.ResendSpanMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.UnsignedSubscriptionMessage
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.service.SubscriptionService
import com.hookiesolutions.webhookie.subscription.service.factory.ConversionsFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.core.GenericSelector
import org.springframework.integration.transformer.GenericTransformer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.Duration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
@Configuration
class SubscriptionConfig(
  private val timeMachine: TimeMachine,
  private val factory: ConversionsFactory,
  private val subscriptionService: SubscriptionService
) {
  @Bean
  fun delayCalculator(
    properties: SubscriptionProperties
  ): GenericTransformer<SignableSubscriptionMessage, Duration> {
    return GenericTransformer {
      Duration.ofSeconds(it.delay.seconds * properties.retry.multiplier + properties.retry.initialInterval)
    }
  }

  @Bean
  fun subscriptionHasReachedMaximumRetrySelector(
    properties: SubscriptionProperties
  ): GenericSelector<GenericPublisherMessage> {
    return GenericSelector {
      it.subscriptionMessage.numberOfRetries >= properties.retry.maxRetry
    }
  }

  @Bean
  fun requiresRetrySelector(
    subscriptionHasReachedMaximumRetrySelector: GenericSelector<GenericPublisherMessage>,
    retryableErrorSelector: GenericSelector<GenericPublisherMessage>
  ): GenericSelector<PublisherErrorMessage> {
    return GenericSelector {
      retryableErrorSelector.accept(it) && !subscriptionHasReachedMaximumRetrySelector.accept(it)
    }
  }

  @Bean
  fun requiresBlockSelector(
    subscriptionHasReachedMaximumRetrySelector: GenericSelector<GenericPublisherMessage>,
    retryableErrorSelector: GenericSelector<GenericPublisherMessage>
  ): GenericSelector<PublisherErrorMessage> {
    return GenericSelector {
      retryableErrorSelector.accept(it) && subscriptionHasReachedMaximumRetrySelector.accept(it)
    }
  }

  @Bean
  fun toDelayedSignableSubscriptionMessage(
    delayCalculator: GenericTransformer<SignableSubscriptionMessage, Duration>
  ): GenericTransformer<PublisherErrorMessage, SignableSubscriptionMessage> {
    return GenericTransformer {
      val subscriptionMessage: SignableSubscriptionMessage = it.subscriptionMessage
      it.subscriptionMessage.retryableCopy(
        delay = delayCalculator.transform(subscriptionMessage),
        numberOfRetries = subscriptionMessage.numberOfRetries + 1
      )
    }
  }

  @Bean
  fun toSubscriptionMessageFlux(): GenericTransformer<ConsumerMessage, Flux<GenericSubscriptionMessage>> {
    return GenericTransformer { cm ->
      subscriptionService.findSubscriptionsFor(cm)
        .map { factory.subscriptionToSubscriptionMessage(it, cm) }
        .switchIfEmpty(NoSubscriptionMessage(cm).toMono())
        .onErrorMap { SubscriptionMessageHandlingException(it.localizedMessage, cm.traceId) }
    }
  }

  @Bean
  fun blockSubscription(): GenericTransformer<PublisherErrorMessage, Mono<BlockedSubscriptionMessageDTO>> {
    return GenericTransformer { payload ->
      subscriptionService.blockSubscription(payload)
        .map { factory.createBlockedSubscriptionMessageDTO(payload, it) }
        .onErrorMap { SubscriptionMessageHandlingException(it.localizedMessage, payload.traceId, payload.spanId) }
    }
  }

  @Bean
  fun toBlockedSubscriptionMessageDTO(): GenericTransformer<UnsignedSubscriptionMessage, BlockedSubscriptionMessageDTO> {
    return GenericTransformer {
      factory.createBlockedSubscriptionMessageDTO(it, timeMachine.now(), "New Message")
    }
  }

  @Bean
  fun toBlockedSubscriptionMessageFlux(): GenericTransformer<Subscription, Flux<BlockedSubscriptionMessage>> {
    return GenericTransformer { subscription ->
      subscriptionService.findAllBlockedMessagesForSubscription(subscription.id!!)
        .onErrorMap { SubscriptionMessageHandlingException(it.localizedMessage) }
    }
  }

  @Bean
  fun signSubscriptionMessage(): GenericTransformer<SignableSubscriptionMessage, Mono<SignableSubscriptionMessage>> {
    return GenericTransformer { msg ->
      subscriptionService.signSubscriptionMessage(msg)
        .onErrorMap { SubscriptionMessageHandlingException(it.localizedMessage, msg.traceId, msg.spanId) }
    }
  }

  @Bean
  fun saveBlockedMessage(): GenericTransformer<BlockedSubscriptionMessageDTO, Mono<BlockedSubscriptionMessage>> {
    return GenericTransformer { bsmDTO ->
      val msg = factory.bmsDTOToBlockedSubscriptionMessage(bsmDTO)
      subscriptionService.saveBlockedSubscriptionMessage(msg)
        .onErrorMap { SubscriptionMessageHandlingException(it.localizedMessage, msg.traceId()) }
    }
  }

  @Bean
  fun deleteBlockedMessage(): GenericTransformer<BlockedSubscriptionMessage, Mono<BlockedSubscriptionMessage>> {
    return GenericTransformer { bsm ->
      subscriptionService.deleteBlockedMessage(bsm)
        .onErrorMap { SubscriptionMessageHandlingException(it.localizedMessage, bsm.traceId()) }
    }
  }

  @Bean
  fun toSignableSubscriptionMessageReloadingSubscription(): GenericTransformer<BlockedSubscriptionMessage, Mono<SignableSubscriptionMessage>> {
    return GenericTransformer { bsm ->
      subscriptionService
        .enrichBlockedSubscriptionMessageReloadingSubscription(bsm)
        .map { factory.blockedSubscriptionMessageToSubscriptionMessage(it) }
    }
  }

  @Bean
  fun toSignableSubscriptionMessageReloadingSubscriptionForResend(): GenericTransformer<ResendSpanMessage, Mono<SignableSubscriptionMessage>> {
    return GenericTransformer { bsm ->
      subscriptionService
        .enrichResendSpanMessageReloadingSubscription(bsm)
    }
  }

  @Bean
  fun messageHasNoSubscription(): (GenericSubscriptionMessage) -> Boolean {
    return { it is NoSubscriptionMessage }
  }

  @Bean
  fun subscriptionIsBlocked(): (GenericSubscriptionMessage) -> Boolean {
    return { it is SignableSubscriptionMessage && it.subscriptionIsBlocked }
  }

  @Bean
  fun subscriptionIsWorking(): (GenericSubscriptionMessage) -> Boolean {
    return { it is SignableSubscriptionMessage && it.subscriptionIsWorking }
  }

  @Bean
  fun toBeSignedWorkingSubscription(
    subscriptionIsWorking: (GenericSubscriptionMessage) -> Boolean
  ): (GenericSubscriptionMessage) -> Boolean {
    return { subscriptionIsWorking.invoke(it) && (it is SignableSubscriptionMessage) && it.isSignable }
  }

  @Bean
  fun nonSignableWorkingSubscription(
    subscriptionIsWorking: (GenericSubscriptionMessage) -> Boolean
  ): (GenericSubscriptionMessage) -> Boolean {
    return { subscriptionIsWorking.invoke(it) && (it is SignableSubscriptionMessage) && !it.isSignable }
  }
}
