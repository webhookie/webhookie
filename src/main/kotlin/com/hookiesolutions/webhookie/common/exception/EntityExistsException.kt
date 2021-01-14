package com.hookiesolutions.webhookie.common.exception

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 19:41
 */
class EntityExistsException(
  val key: String,
  message: String
): RuntimeException(message)