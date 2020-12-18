package com.hookiesolutions.webhookie.config.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "webhookie.security")
@Component
class WebHookieSecurityProperties(
  var noAuth: NoAuth = NoAuth()
) {

  class NoAuth {
    var pathMatchers = mapOf<String,Array<String>>()
  }
}
