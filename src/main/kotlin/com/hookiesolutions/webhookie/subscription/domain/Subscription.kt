package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.http.HttpMethod

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:27
 */
data class Subscription(
  val name: String,
  @Indexed
  val topic: String,
  val callbackUrl: String,
  val httpMethod: HttpMethod,
  val securitySchema: CallbackSecuritySchema
) : AbstractEntity()
