package com.hookiesolutions.webhookie.subscription.service.state

import com.hookiesolutions.webhookie.common.extension.capitalize
import com.hookiesolutions.webhookie.common.model.dto.SubscriptionStatus
import com.hookiesolutions.webhookie.common.model.dto.WebhookApiDetails
import com.hookiesolutions.webhookie.subscription.domain.Subscription
import com.hookiesolutions.webhookie.webhook.service.WebhookApiServiceDelegate
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class SubscriptionStateManager(
  private val webhookServiceDelegate: WebhookApiServiceDelegate,
  private val openWebhookSubscriptionStateManager: OpenWebhookSubscriptionStateManager,
  private val closedWebhookSubscriptionStateManager: ClosedWebhookSubscriptionStateManager
) {
  fun canBeDeleted(subscription: Subscription): Mono<Subscription> {
    return if(subscription.statusUpdate.status != SubscriptionStatus.ACTIVATED) {
      subscription.toMono()
    } else {
      Mono.error(AccessDeniedException("${SubscriptionStatus.ACTIVATED.name} subscription cannot be deleted!"))
    }
  }

  fun canBeVerified(subscription: Subscription): Mono<SubscriptionStatusUpdate> {
    return webhookServiceDelegate.webhookApiDetailsByTopic(subscription.topic)
      .flatMap { apiDetails ->
        val toBeStatus = if(apiDetails.requiresApproval) {
          SubscriptionStatus.READY_TO_SUBMIT
        } else {
          SubscriptionStatus.VALIDATED
        }
        verifyAction(subscription, toBeStatus, apiDetails)
      }
  }

  fun canBeActivated(subscription: Subscription): Mono<SubscriptionStatusUpdate> {
    return webhookServiceDelegate.webhookApiDetailsByTopic(subscription.topic)
      .flatMap { verifyAction(subscription, SubscriptionStatus.ACTIVATED, it) }
  }

  fun canBeSubmitted(subscription: Subscription): Mono<SubscriptionStatusUpdate> {
    return webhookServiceDelegate.webhookApiDetailsByTopic(subscription.topic)
      .flatMap { verifyAction(subscription, SubscriptionStatus.SUBMITTED, it) }
  }

  fun canBeApproved(subscription: Subscription): Mono<SubscriptionStatusUpdate> {
    return webhookServiceDelegate.webhookApiDetailsByTopic(subscription.topic)
      .flatMap { verifyAction(subscription, SubscriptionStatus.APPROVED, it) }
  }

  fun canBeRejected(subscription: Subscription): Mono<SubscriptionStatusUpdate> {
    return webhookServiceDelegate.webhookApiDetailsByTopic(subscription.topic)
      .flatMap { verifyAction(subscription, SubscriptionStatus.REJECTED, it) }
  }

  fun canBeDeactivated(subscription: Subscription): Mono<SubscriptionStatusUpdate> {
    return verifyAction(subscription, SubscriptionStatus.DEACTIVATED, null)
  }

  fun canBeSuspended(subscription: Subscription): Mono<SubscriptionStatusUpdate> {
    return verifyAction(subscription, SubscriptionStatus.SUSPENDED, null)
  }

  fun canBeUnsuspended(subscription: Subscription): Mono<SubscriptionStatusUpdate> {
    return verifyAction(subscription, SubscriptionStatus.DEACTIVATED, null)
  }

  private fun verifyAction(subscription: Subscription, status: SubscriptionStatus, webhookApiDetails: WebhookApiDetails?): Mono<SubscriptionStatusUpdate> {
    val stateManager = if(webhookApiDetails != null && webhookApiDetails.requiresApproval) {
      closedWebhookSubscriptionStateManager
    } else {
      openWebhookSubscriptionStateManager
    }
    val validStatusList = stateManager.statusTransitionMap[status] ?: emptyList()
    return if (validStatusList.contains(subscription.statusUpdate.status)) {
      SubscriptionStatusUpdate(subscription, validStatusList, status).toMono()
    } else {
      val msg = "${subscription.statusUpdate.capitalizedName()} Subscription cannot be ${status.capitalize()}"
      Mono.error(IllegalArgumentException(msg))
    }
  }

  fun canBeUpdated(subscription: Subscription): Mono<Subscription> {
    val editableStatusList = listOf(
      SubscriptionStatus.DRAFT,
      SubscriptionStatus.READY_TO_SUBMIT,
      SubscriptionStatus.VALIDATED
    )
    return if(subscription.statusUpdate.status in editableStatusList) {
      subscription.toMono()
    } else {
      Mono.error(AccessDeniedException("only $editableStatusList subscriptions can be updated"))
    }
  }
}
