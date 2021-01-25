package com.hookiesolutions.webhookie.common.config.web

import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Pageable
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver
import org.springframework.data.web.ReactiveSortHandlerMethodArgumentResolver


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
}