package com.hookiesolutions.webhookie.common.message.publisher

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 18/12/20 03:16
 */
data class ServerResponse(
  val status: HttpStatus,
  val data: ByteArray,
  val headers: HttpHeaders
) {
  fun is5xxServerError(): Boolean = status.is5xxServerError

  fun isNotFound(): Boolean = status == HttpStatus.NOT_FOUND

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ServerResponse) return false

    if (status != other.status) return false
    if (!data.contentEquals(other.data)) return false
    if (headers != other.headers) return false

    return true
  }

  override fun hashCode(): Int {
    var result = status.hashCode()
    result = 31 * result + data.contentHashCode()
    result = 31 * result + headers.hashCode()
    return result
  }
}
