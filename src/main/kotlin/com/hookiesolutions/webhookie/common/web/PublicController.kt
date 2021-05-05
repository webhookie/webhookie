package com.hookiesolutions.webhookie.common.web

import com.hookiesolutions.webhookie.common.web.CommonAPIDocs.Companion.REQUEST_MAPPING_PUBLIC
import com.hookiesolutions.webhookie.security.WebHookieSecurityProperties
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 18/2/21 21:51
 */
@RestController
@RequestMapping(REQUEST_MAPPING_PUBLIC)
class PublicController(
  private val securityProperties: WebHookieSecurityProperties,
  private val oAuth2ResourceServerProperties: OAuth2ResourceServerProperties
) {
  @GetMapping(
    "/config",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun iamConfig(): Mono<WebhookieConfig> {
    return WebhookieConfig.builder()
      .issuer(oAuth2ResourceServerProperties.jwt.issuerUri)
      .clientId(securityProperties.clientId)
      .audience(securityProperties.audience)
      .build()
      .toMono()
  }

}

data class WebhookieConfig(
  val iam: IAMConfig
) {
  data class IAMConfig(
    val issuer: String,
    val clientId: String,
    val audience: String
  )

  companion object {
    fun builder() = Builder()
  }

  class Builder {
    private lateinit var issuer: String
    private lateinit var clientId: String
    private lateinit var audience: String

    fun issuer(value: String) = apply { this.issuer = value }
    fun clientId(value: String) = apply { this.clientId = value }
    fun audience(value: String) = apply { this.audience = value }

    fun build() = WebhookieConfig(
      IAMConfig(issuer, clientId, audience)
    )
  }
}
