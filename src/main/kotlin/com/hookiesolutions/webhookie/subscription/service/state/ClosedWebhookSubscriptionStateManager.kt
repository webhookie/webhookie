package com.hookiesolutions.webhookie.subscription.service.state

import com.hookiesolutions.webhookie.common.model.dto.SubscriptionStatus
import org.springframework.stereotype.Service

@Service
class ClosedWebhookSubscriptionStateManager: AbstractSubscriptionStateManager() {
  override val statusTransitionMap: Map<SubscriptionStatus, List<SubscriptionStatus>> = basicStatusTransitionMap.plus(
    listOf (
      SubscriptionStatus.READY_TO_SUBMIT to listOf(
        SubscriptionStatus.DRAFT,
        SubscriptionStatus.READY_TO_SUBMIT
      ),
      SubscriptionStatus.SUBMITTED to listOf(SubscriptionStatus.READY_TO_SUBMIT),
      SubscriptionStatus.APPROVED to listOf(SubscriptionStatus.SUBMITTED),
      SubscriptionStatus.REJECTED to listOf(SubscriptionStatus.SUBMITTED),
      SubscriptionStatus.ACTIVATED to listOf(SubscriptionStatus.APPROVED, SubscriptionStatus.DEACTIVATED)
    )
  )
}
