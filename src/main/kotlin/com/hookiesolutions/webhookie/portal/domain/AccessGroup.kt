package com.hookiesolutions.webhookie.portal.domain


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 14:05
 */
interface AccessGroup {
  val name: String
  val description: String
  val iamGroupName: String
  val enabled: Boolean
}