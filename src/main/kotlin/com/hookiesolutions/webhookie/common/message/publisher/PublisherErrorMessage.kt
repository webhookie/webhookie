package com.hookiesolutions.webhookie.common.message.publisher

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 18/12/20 02:59
 */
interface PublisherErrorMessage: GenericPublisherMessage {
  val reason: String
}