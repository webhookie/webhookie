package com.hookiesolutions.webhookie.common.health

import com.hookiesolutions.webhookie.common.exception.RemoteServiceException
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 9/7/21 00:21
 */
@Component
class IAMHealthIndicator(
  private val jwtKeySetWebClient: WebClient,
  private val resourceServerProperties: OAuth2ResourceServerProperties
): ReactiveHealthIndicator {
  override fun health(): Mono<Health> {
    return jwtKeySetWebClient
      .get()
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(Any::class.java)
      .flatMap { up() }
      .onErrorResume(WebClientRequestException::class.java) {
        RemoteServiceException("Unable to communicate to the spec parser! Please contact Administrator").toMono()
      }
      .onErrorResume { down(it) }
  }

  private fun up(): Mono<Health> {
    return Health
      .up()
      .withDetail("jwt", resourceServerProperties.jwt)
      .build()
      .toMono()
  }

  private fun down(ex: Throwable): Mono<Health> {
    return         Health
      .down()
      .withDetail("reason", ex.localizedMessage)
      .build()
      .toMono()
  }
}

