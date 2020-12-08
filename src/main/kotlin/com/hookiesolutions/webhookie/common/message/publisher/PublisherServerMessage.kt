package com.hookiesolutions.webhookie.common.message.publisher

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/12/20 23:47
 */
interface PublisherServerMessage: GenericPublisherMessage {
  val status: HttpStatus
  val response: ByteArray
  val headers: HttpHeaders
}