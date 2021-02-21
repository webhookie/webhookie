package com.hookiesolutions.webhookie.common.config.web

import com.hookiesolutions.webhookie.common.service.TimeMachine
import org.slf4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.data.domain.Pageable
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver
import org.springframework.data.web.ReactiveSortHandlerMethodArgumentResolver
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import reactor.core.publisher.Mono
import java.time.Instant


/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/1/21 13:58
 */
@Configuration
@EnableWebFlux
class WebConfig: WebFluxConfigurer {
  override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
    val pageableArgumentResolver =
      ReactivePageableHandlerMethodArgumentResolver(ReactiveSortHandlerMethodArgumentResolver())
    pageableArgumentResolver.setFallbackPageable(Pageable.unpaged())
    configurer.addCustomResolver(pageableArgumentResolver)
  }

  @Bean
  @Order(-1)
  @Profile(value = ["!prod"])
  fun logGatewayRequest(
    log: Logger,
    timeMachine: TimeMachine
  ): WebFilter {
    return WebFilter { exchange, chain ->
      val filterMono = chain.filter(exchange)
      return@WebFilter if (log.isDebugEnabled) {
        filterMono
          .then(debugRequestResponse(exchange, log, timeMachine.now(), timeMachine))
      } else {
        filterMono
      }
    }
  }

  private fun debugRequestResponse(
    exchange: ServerWebExchange,
    log: Logger,
    start: Instant,
    timeMachine: TimeMachine,
  ): Mono<Void> = Mono.fromRunnable {
    val finish = timeMachine.now()
    val request = exchange.request
    val response = exchange.response
    if (log.isDebugEnabled) {
      log.debug("'${request.methodValue} ${request.uri} ${request.id}' {} {} ms",
        response.statusCode?.value(),
        finish.toEpochMilli() - start.toEpochMilli())
    }
    if (log.isTraceEnabled) {
      val requestHeaders = request.headers
        .mapValues {
          val value = if (it.key != HttpHeaders.AUTHORIZATION) {
            it.value.toString()
          } else {
            "*****"
          }

          value
        }
      log.trace("Request headers: {}", requestHeaders)
      log.trace("Response headers: {}", response.headers)
    }
  }
}
