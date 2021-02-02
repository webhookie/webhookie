package com.hookiesolutions.webhookie.common.model.dto

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 7/12/20 01:25
 */
data class SubscriptionDTO(
  val id: String,
  val name: String,
  val entity: String,
  val applicationId: String,
  val topic: String,
  val callback: CallbackDTO,
  val blockedDetails: BlockedDetailsDTO? = null
) {
  val isBlocked: Boolean
    get() = blockedDetails != null
}
