package com.hookiesolutions.webhookie.config.security

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
data class JwtAttributes(
  val aud: String,
  val roles: List<String>
)
