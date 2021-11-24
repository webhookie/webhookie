package com.hookiesolutions.webhookie.subscription.domain.callback.security

import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document
@TypeAlias("none_security_scheme")
class NoneSecurityScheme: CallbackSecurityScheme() {
  override val type: SecuritySchemeType
    get() = SecuritySchemeType.NONE
}
