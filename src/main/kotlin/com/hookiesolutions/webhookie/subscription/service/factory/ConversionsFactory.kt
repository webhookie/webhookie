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

package com.hookiesolutions.webhookie.subscription.service.factory

import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherErrorMessage
import com.hookiesolutions.webhookie.common.message.subscription.BlockedSubscriptionMessageDTO
import com.hookiesolutions.webhookie.common.message.subscription.GenericSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignableSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SignedSubscriptionMessage
import com.hookiesolutions.webhookie.common.message.subscription.SubscriptionSignature
import com.hookiesolutions.webhookie.common.message.subscription.UnsignedSubscriptionMessage
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate.Companion.blocked
import com.hookiesolutions.webhookie.common.model.dto.StatusUpdate.Companion.saved
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionDTO
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionStatus
import com.hookiesolutions.webhookie.common.service.IdGenerator
import com.hookiesolutions.webhookie.common.service.TimeMachine
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.BlockedSubscriptionMessage
import com.hookiesolutions.webhookie.subscription.domain.callback.Callback
import com.hookiesolutions.webhookie.subscription.domain.callback.CallbackDetails
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.subscription.service.model.ApplicationRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.CreateSubscriptionRequest
import com.hookiesolutions.webhookie.subscription.service.model.subscription.UpdateSubscriptionRequest
import org.springframework.stereotype.Service
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/1/21 12:24
 */
@Service
class ConversionsFactory(
  private val idGenerator: IdGenerator,
  private val timeMachine: TimeMachine
) {
  fun createApplicationRequestToApplication(
    request: ApplicationRequest,
    entity: String,
    creatorUserId: String,
    ): Application {
    return Application(request.name, request.description, entity, setOf(creatorUserId))
  }

  fun blockedSubscriptionMessageToSubscriptionMessage(
    bsm: BlockedSubscriptionMessage
  ): UnsignedSubscriptionMessage {
    return UnsignedSubscriptionMessage(
      originalMessage = bsm.consumerMessage,
      spanId = bsm.spanId,
      subscription = bsm.subscription,
      totalNumberOfTries = bsm.totalNumberOfTries
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
      dto.spanId,
      dto.consumerMessage,
      dto.subscription,
      dto.totalNumberOfTries,
      dto.blockedDetails
    )
  }

  fun createBlockedSubscriptionMessageDTO(
    errorMessage: PublisherErrorMessage,
    statusUpdate: StatusUpdate
  ): BlockedSubscriptionMessageDTO {
    val originalMessage = errorMessage.subscriptionMessage.originalMessage
    return BlockedSubscriptionMessageDTO(
      null,
      errorMessage.spanId,
      originalMessage,
      errorMessage.subscriptionMessage.subscription,
      errorMessage.subscriptionMessage.totalNumberOfTries,
      errorMessage.subscriptionMessage.numberOfRetries,
      statusUpdate
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
      message.spanId,
      originalMessage,
      message.subscription,
      message.totalNumberOfTries,
      message.numberOfRetries,
      blocked(at, reason)
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
    totalNumberOfTries = subscriptionMessage.totalNumberOfTries,
    signature = signature,
  )

  fun updateBlockedSubscriptionMessageWithSubscription(
    message: BlockedSubscriptionMessage,
    subscription: SubscriptionDTO,
  ): BlockedSubscriptionMessage {
    val copy = message.copy(subscription = subscription)
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
      application.details(),
      callback.details(),
      saved(timeMachine.now())
    )
  }

  fun modifySubscription(
    application: Application,
    callback: Callback,
    subscription: Subscription,
    request: UpdateSubscriptionRequest,
    at: Instant
  ): Subscription {
    val copy = subscription.copy(
      application = application.details(),
      callback = CallbackDetails(callback.id!!, callback.name, callback.httpMethod, callback.url, callback.securityScheme),
      statusUpdate = StatusUpdate(SubscriptionStatus.DRAFT, "Edit Subscription", at)
    )
    copy.id = subscription.id
    copy.createdBy = subscription.createdBy
    copy.createdDate = subscription.createdDate
    copy.lastModifiedDate = subscription.lastModifiedDate
    copy.lastModifiedBy = subscription.lastModifiedBy

    return copy
  }
}
