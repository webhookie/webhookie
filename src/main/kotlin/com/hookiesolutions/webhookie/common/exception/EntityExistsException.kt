package com.hookiesolutions.webhookie.common.exception

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 19:41
 */
class EntityExistsException(
  override val message: String
): RuntimeException(message) {
  val key: String
    get() = message.substringAfter("dup key:")
}