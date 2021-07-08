package com.hookiesolutions.webhookie.webhook.config.parser

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 25/5/21 01:16
 */
@Configuration
class WebClientConfig(
  private val parserServiceProperties: ParserServiceProperties
) {
  @Bean
  fun webClientBuilder(): WebClient.Builder {
    return WebClient.builder()
  }

  @Bean
  fun parserWebClient(webClientBuilder: WebClient.Builder): WebClient {
    return webClientBuilder
      .baseUrl(parserServiceProperties.url)
      .build()
  }

  @Bean
  fun jwtKeySetWebClient(
    webClientBuilder: WebClient.Builder,
    resourceServerProperties: OAuth2ResourceServerProperties
  ): WebClient {
    return webClientBuilder
      .baseUrl(resourceServerProperties.jwt.jwkSetUri)
      .build()
  }
}
