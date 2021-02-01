package com.hookiesolutions.webhookie.common.message.entity

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 22/1/21 14:50
 */
data class EntityUpdatedMessage<T>(
  override val type: String,
  val oldValue: T,
  val newValue: T,
): EntityModifiedMessage {
  fun hasChanges(): Boolean {
    return oldValue != newValue
  }
}