package com.hookiesolutions.webhookie.subscription.config

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.GenericSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.NoSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionMessage
import com.hookiesolutions.webhookie.common.service.IdGenerator
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.service.SubscriptionService
import com.hookiesolutions.webhookie.subscription.service.SubscriptionSignor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.transformer.GenericTransformer
import org.springframework.messaging.MessageHeaders
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
@Configuration
class SubscriptionConfig(
  private val timeMachine: TimeMachine,
  private val idGenerator: IdGenerator,
  private val signor: SubscriptionSignor,
  private val subscriptionService: SubscriptionService
) {
  @Bean
  fun toSubscriptionMessageFlux(): GenericTransformer<ConsumerMessage, Flux<GenericSubscriptionMessage>> {
    return GenericTransformer {  cm ->
      subscriptionService.findSubscriptionsFor(cm)
        .map {
          val spanId = idGenerator.generate()
          val signature = signor.sign(it, cm, spanId)
          it.subscriptionMessage(cm, spanId, signature)
        }
        .switchIfEmpty(NoSubscriptionMessage(cm).toMono())
    }
  }

  @Bean
  fun blockSubscription(): GenericTransformer<PublisherErrorMessage, Mono<BlockedSubscriptionMessageDTO>> {
    return GenericTransformer { payload ->
        subscriptionService.blockSubscriptionFor(payload)
          .map { BlockedSubscriptionMessageDTO.from(payload, it) }
    }
  }

  @Bean
  fun toBlockedSubscriptionMessageDTO(): GenericTransformer<SubscriptionMessage, BlockedSubscriptionMessageDTO> {
    return GenericTransformer {
      BlockedSubscriptionMessageDTO.from(it, timeMachine.now(), "New Message")
    }
  }

  @Bean
  fun toBlockedSubscriptionMessageFlux(): GenericTransformer<Subscription, Flux<BlockedSubscriptionMessage>> {
    return GenericTransformer {
      subscriptionService.findAllBlockedMessagesForSubscription(it.id!!)
    }
  }

  @Bean
  fun saveBlockedMessageMono(): GenericTransformer<BlockedSubscriptionMessageDTO, Mono<BlockedSubscriptionMessage>> {
    return GenericTransformer {
      subscriptionService.saveBlockedSubscriptionMessage(BlockedSubscriptionMessage.from(it))
    }
  }

  @Bean
  fun resendAndRemoveSingleBlockedMessage(): (BlockedSubscriptionMessage, MessageHeaders) -> Unit {
    return { payload: BlockedSubscriptionMessage, _: MessageHeaders ->
      subscriptionService.resendAndRemoveSingleBlockedMessage(payload)
    }
  }

  @Bean
  fun messageHasNoSubscription(): (GenericSubscriptionMessage) -> Boolean {
    return { it is NoSubscriptionMessage }
  }

  @Bean
  fun subscriptionIsBlocked(): (GenericSubscriptionMessage) -> Boolean {
    return { it is SubscriptionMessage && it.subscriptionIsBlocked }
  }

  @Bean
  fun subscriptionIsWorking(): (GenericSubscriptionMessage) -> Boolean {
    return { it is SubscriptionMessage && it.subscriptionIsWorking }
  }
}
