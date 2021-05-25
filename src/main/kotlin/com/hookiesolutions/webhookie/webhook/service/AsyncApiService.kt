package com.hookiesolutions.webhookie.webhook.service

import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup
import com.hookiesolutions.webhookie.webhook.service.model.AsyncApiSpec
import com.hookiesolutions.webhookie.webhook.service.model.WebhookGroupRequest
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 25/5/21 02:33
 */
@Service
class AsyncApiService(
  private val parserWebClient: WebClient,
  private val log: Logger
) {
  fun parseAsyncApiSpecToWebhookApi(request: WebhookGroupRequest): Mono<WebhookGroup> {
    return parserWebClient
      .post()
      .uri("/parse")
      .contentType(MediaType.parseMediaType("text/yaml"))
      .body(BodyInserters.fromValue(request.asyncApiSpec))
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(AsyncApiSpec::class.java)
      .doOnNext { log.info("AsyncAPI Spec parsed successfully. number of topics: '{}'", it.topics.size) }
      .doOnError { log.error("AsyncAPI Spec parse error '{}'", it.message) }
      .map {
        WebhookGroup(
          it.name,
          it.version,
          it.description,
          it.topics,
          request.asyncApiSpec,
          request.consumerGroups,
          request.providerGroups,
          request.consumerAccess,
          request.providerAccess
        )
      }
  }
}
