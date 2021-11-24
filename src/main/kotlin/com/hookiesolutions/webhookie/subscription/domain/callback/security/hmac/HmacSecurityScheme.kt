package com.hookiesolutions.webhookie.subscription.domain.callback.security.hmac

import com.hookiesolutions.webhookie.subscription.domain.callback.security.CallbackSecurityScheme
import com.hookiesolutions.webhookie.subscription.domain.callback.security.SecuritySchemeType
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document
@TypeAlias("hmac_security_scheme")
data class HmacSecurityScheme(
  val secret: HmacSecret
): CallbackSecurityScheme() {
  override val type: SecuritySchemeType
    get() = SecuritySchemeType.HMAC
}
