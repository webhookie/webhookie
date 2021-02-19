package com.hookiesolutions.webhookie.common.config.web

import org.slf4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.data.domain.Pageable
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver
import org.springframework.data.web.ReactiveSortHandlerMethodArgumentResolver
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
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
  @Profile(value = ["!prod"])
  fun corsFilter(): CorsWebFilter {
    val config = CorsConfiguration()

    config.allowCredentials = true
    config.addAllowedOrigin(FE_DEV_ORIGIN)
    config.addAllowedHeader("*")
    config.addAllowedMethod("*")

    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", config)

    return CorsWebFilter(source)
  }

  @Bean
  @Order(-1)
  @Profile(value = ["!prod"])
  fun logGatewayRequest(log: Logger): WebFilter {
    return WebFilter { exchange, chain ->
      val start = Instant.now()
      return@WebFilter chain.filter(exchange)
        .then(Mono.fromRunnable<Void> {
          val finish = Instant.now()
          val request = exchange.request
          val response = exchange.response
          log.debug("\"${request.methodValue} ${request.uri} ${request.id}\" {} {} ms",
            response.statusCode?.value(),
            finish.toEpochMilli() - start.toEpochMilli())
        })
    }
  }

  companion object {
    const val FE_DEV_ORIGIN = "http://localhost:4200"
  }
}
