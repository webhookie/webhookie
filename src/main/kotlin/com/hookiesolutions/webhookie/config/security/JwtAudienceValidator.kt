package com.hookiesolutions.webhookie.config.security

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since ${DATE} ${TIME}
 */
@Component
class AudienceValidator(
  private val securityProperties: WebHookieSecurityProperties,
) : OAuth2TokenValidator<Jwt> {
  var error: OAuth2Error = OAuth2Error("invalid_token", "The required audience is missing", null)

  override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
    return if (jwt.audience.contains(securityProperties.audience)) {
      OAuth2TokenValidatorResult.success()
    } else {
      OAuth2TokenValidatorResult.failure(error)
    }
  }
}