package com.hookiesolutions.webhookie.webhook.config

import com.hookiesolutions.webhookie.webhook.web.reader.WebhookGroupRequestReader
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.config.WebFluxConfigurer

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 12:23
 */
@Configuration
class WebhookWebConfig(
  private val reader: WebhookGroupRequestReader,
): WebFluxConfigurer {
  override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
    configurer.customCodecs().register(reader)

    super.configureHttpMessageCodecs(configurer)
  }
}