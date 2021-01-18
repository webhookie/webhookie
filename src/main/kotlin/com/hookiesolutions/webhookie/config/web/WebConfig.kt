package com.hookiesolutions.webhookie.config.web

import com.hookiesolutions.webhookie.portal.web.io.AsyncDocumentEncoder
import com.hookiesolutions.webhookie.portal.web.io.AsyncDocumentReader
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/1/21 13:58
 */
@Configuration
@EnableWebFlux
class WebConfig(
  private val reader: AsyncDocumentReader,
  private val encoder: AsyncDocumentEncoder
): WebFluxConfigurer {
  override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
    configurer.customCodecs().register(reader)
    configurer.customCodecs().register(encoder)

    super.configureHttpMessageCodecs(configurer)
  }
}