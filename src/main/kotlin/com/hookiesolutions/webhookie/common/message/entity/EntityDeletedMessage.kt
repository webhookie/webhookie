package com.hookiesolutions.webhookie.common.message.entity

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 22/1/21 13:04
 */
data class EntityDeletedMessage<T>(
  override val type: String,
  val value: T
): EntityModifiedMessage