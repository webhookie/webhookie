package com.hookiesolutions.webhookie.common.health

import com.hookiesolutions.webhookie.common.exception.RemoteServiceException
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
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
): ReactiveHealthIndicator {
  override fun health(): Mono<Health> {
    return jwtKeySetWebClient
      .get()
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(Any::class.java)
      .flatMap { Health.up().build().toMono() }
      .onErrorResume(WebClientRequestException::class.java) {
        RemoteServiceException("Unable to communicate to the spec parser! Please contact Administrator").toMono()
      }
      .onErrorResume {
        Health
          .down()
          .withDetail("reason", it.localizedMessage)
          .build()
          .toMono()
      }
  }
}

