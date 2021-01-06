package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.GenericSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.UnsignedSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.dto.BlockedDetailsDTO
import com.hookiesolutions.webhookie.common.service.IdGenerator
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.Company
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.service.model.CreateApplicationRequest
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
  fun createSubscriptionRequestToSubscription(
    request: CreateSubscriptionRequest,
    application: Application
  ): Subscription {
    return Subscription(
      request.name,
      application.companyId,
      application.id!!,
      request.topic,
      request.callbackUrl,
      request.httpMethod,
      request.callbackSecurity
    )
  }

  fun createApplicationRequestToApplication(
    request: CreateApplicationRequest,
    company: Company
  ): Application {
    return Application(request.name, company.id!!)
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
}