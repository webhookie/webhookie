package com.hookiesolutions.webhookie.security

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/2/21 02:17
 */
data class TokenData(
  val entity: String,
  val groups: List<String>
)
