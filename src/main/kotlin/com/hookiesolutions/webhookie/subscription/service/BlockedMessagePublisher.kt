package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.exception.SubscriptionException
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 27/1/21 18:52
 */
@Service
class BlockedMessagePublisher(
  private val factory: ConversionsFactory,
  private val signSubscriptionMessageChannel: MessageChannel,
  private val subscriptionChannel: MessageChannel,
) {
  fun resendBlockedSubscriptionMessage(bsm: BlockedSubscriptionMessage, subscription: Subscription): Mono<Boolean> {
    val subscriptionMessage = factory.blockedSubscriptionMessageToSubscriptionMessage(bsm, subscription)
    val message = MessageBuilder
      .withPayload(subscriptionMessage)
      .copyHeadersIfAbsent(bsm.messageHeaders)
      .build()

    val sent = outputChannel(bsm.subscription)
      .send(message)
    return if (sent) {
      sent.toMono()
    } else {
      Mono.error(SubscriptionException("Unable to publish blocked message: ${bsm.id}"))
    }
  }

  fun outputChannel(subscription: SubscriptionDTO): MessageChannel {
    return if(subscription.callback.security == null) {
      subscriptionChannel
    } else {
      signSubscriptionMessageChannel
    }
  }
}