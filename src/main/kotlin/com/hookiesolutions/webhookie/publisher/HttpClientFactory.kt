package com.hookiesolutions.webhookie.publisher

import org.slf4j.Logger
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 7/12/20 16:00
 */
@Service
class HttpClientFactory(
  private val log: Logger,
  private val webClientBuilder: WebClient.Builder
) {
  @Cacheable("webClients")
  fun createClientFor(url: String, method: HttpMethod, mediaType: MediaType): WebClient.RequestBodySpec {
    log.info("Creating client for: '{}', '{}', '{}'", method.name, mediaType.toString(), url)

    return webClientBuilder
      .baseUrl(url)
      .build()
      .method(method)
      .contentType(mediaType)
  }
}