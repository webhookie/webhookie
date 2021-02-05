package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.GenericSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignedSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionSignature
import com.hookiesolutions.webhookie.common.message.subscription.UnsignedSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.common.service.IdGenerator
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.ApplicationDetails
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.Callback
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.service.model.ApplicationRequest
import com.hookiesolutions.webhookie.subscription.service.model.CreateSubscriptionRequest
import org.springframework.stereotype.Service
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/1/21 12:24
 */
@Service
class ConversionsFactory(
  private val idGenerator: IdGenerator
) {
  fun createApplicationRequestToApplication(
    request: ApplicationRequest,
    entity: String,
    validConsumerGroups: Set<String>,
    ): Application {
    return Application(request.name, request.description, entity, validConsumerGroups)
  }

  fun blockedSubscriptionMessageToSubscriptionMessage(
    bsm: BlockedSubscriptionMessage
  ): UnsignedSubscriptionMessage {
    return UnsignedSubscriptionMessage(
      originalMessage = bsm.originalMessage,
      spanId = bsm.originalSpanId,
      subscription = bsm.subscription
    )
  }

  fun subscriptionToSubscriptionMessage(
    subscription: Subscription,
    consumerMessage: ConsumerMessage
  ): GenericSubscriptionMessage {
    val spanId = idGenerator.generate()
    return UnsignedSubscriptionMessage(
      originalMessage = consumerMessage,
      spanId = spanId,
      subscription = subscription.dto()
    )
  }

  fun bmsDTOToBlockedSubscriptionMessage(
    dto: BlockedSubscriptionMessageDTO
  ): BlockedSubscriptionMessage {
    return BlockedSubscriptionMessage(
      dto.headers,
      dto.originalSpanId,
      dto.payload,
      dto.messageHeaders,
      dto.subscription,
      dto.blockedDetails
    )
  }

  fun createBlockedSubscriptionMessageDTO(
    errorMessage: PublisherErrorMessage,
    blockedDetailsDTO: BlockedDetailsDTO
  ): BlockedSubscriptionMessageDTO {
    val originalMessage = errorMessage.subscriptionMessage.originalMessage
    return BlockedSubscriptionMessageDTO(
      null,
      originalMessage.headers,
      errorMessage.subscriptionMessage.spanId,
      originalMessage.payload,
      originalMessage.messageHeaders,
      errorMessage.subscriptionMessage.subscription,
      blockedDetailsDTO
    )
  }

  fun createBlockedSubscriptionMessageDTO(
    message: UnsignedSubscriptionMessage,
    at: Instant,
    reason: String
  ): BlockedSubscriptionMessageDTO {
    val originalMessage = message.originalMessage
    return BlockedSubscriptionMessageDTO(
      null,
      originalMessage.headers,
      message.spanId,
      originalMessage.payload,
      originalMessage.messageHeaders,
      message.subscription,
      BlockedDetailsDTO(reason, at)
    )
  }

  fun createSignedSubscriptionMessage(
    subscriptionMessage: SignableSubscriptionMessage,
    signature: SubscriptionSignature,
  ) = SignedSubscriptionMessage(
    originalMessage = subscriptionMessage.originalMessage,
    spanId = signature.spanId,
    subscription = subscriptionMessage.subscription,
    delay = subscriptionMessage.delay,
    numberOfRetries = subscriptionMessage.numberOfRetries,
    signature = signature,
  )

  fun updateBlockedSubscriptionMessageWithSubscription(
    message: BlockedSubscriptionMessage,
    subscription: Subscription,
  ): BlockedSubscriptionMessage {
    val copy = message.copy(subscription = subscription.dto())
    copy.id = message.id
    copy.createdBy = message.createdBy
    copy.createdDate = message.createdDate
    copy.lastModifiedDate = message.lastModifiedDate
    copy.lastModifiedBy = message.lastModifiedBy
    return copy
  }

  fun createSubscription(application: Application, callback: Callback, request: CreateSubscriptionRequest): Subscription {
    return Subscription(
      request.topic,
      ApplicationDetails(callback.applicationId, application.entity),
      callback.details()
    )
  }
}