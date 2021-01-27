package com.hookiesolutions.webhookie.common.model.dto

import com.hookiesolutions.webhookie.subscription.domain.CallbackSecurity
import org.springframework.http.HttpMethod

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 27/1/21 14:09
 */
data class Callback(
  val url: String,
  val httpMethod: HttpMethod,
  val security: CallbackSecurity? = null,
) {
  fun requestTarget(): String {
    return "${httpMethod.name} $url"
  }
}