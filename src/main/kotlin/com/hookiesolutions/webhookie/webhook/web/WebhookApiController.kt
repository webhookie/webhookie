package com.hookiesolutions.webhookie.webhook.web

import com.fasterxml.jackson.annotation.JsonView
import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import com.hookiesolutions.webhookie.webhook.config.WebhookApiAPIDocs.Companion.REQUEST_MAPPING_WEBHOOK_APIS
import com.hookiesolutions.webhookie.webhook.domain.Topic
import com.hookiesolutions.webhookie.webhook.service.WebhookApiService
import com.hookiesolutions.webhookie.webhook.service.model.WebhookApiRequest
import com.hookiesolutions.webhookie.webhook.web.response.WebhookApiResponse
import com.hookiesolutions.webhookie.webhook.web.response.WebhookApiViews
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.validation.Valid

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 15:16
 */
@RestController
@RequestMapping(REQUEST_MAPPING_WEBHOOK_APIS)
@SecurityRequirement(name = OAUTH2_SCHEME)
class WebhookApiController(
  private val service: WebhookApiService
) {
  @PostMapping(
    "",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  @ResponseStatus(HttpStatus.CREATED)
  fun createWebhookApi(@RequestBody @Valid request: WebhookApiRequest): Mono<WebhookApiResponse> {
    return service.createWebhookApi(request)
      .map { WebhookApiResponse(it) }
  }

  @GetMapping(
    "",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  @JsonView(WebhookApiViews.Full::class)
  fun getWebhookApis(pageable: Pageable): Flux<WebhookApiResponse> {
    return service.findMyWebhookApis(pageable)
      .map { WebhookApiResponse(it) }
  }

  @GetMapping(
    "/summary",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  @JsonView(WebhookApiViews.Summary::class)
  fun getWebhookApisSummary(pageable: Pageable): Flux<WebhookApiResponse> {
    return service.findMyWebhookApis(pageable)
      .map { WebhookApiResponse(it) }
  }

  @GetMapping(
    "/{id}",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getWebhookApi(@PathVariable id: String): Mono<WebhookApiResponse> {
    return service.readWebhookApi(id)
      .map { WebhookApiResponse(it) }
  }

  @GetMapping(
    "/byTopic",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getWebhookApiByTopic(@RequestParam topic: String): Mono<WebhookApiResponse> {
    return service.readWebhookApiByTopic(topic)
      .map { WebhookApiResponse(it) }
  }

  @DeleteMapping(
    "/{id}",
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun deleteWebhookApi(@PathVariable id: String): Mono<String> {
    return service.deleteWebhookApi(id)
  }

  @PutMapping(
    "/{id}",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun updateWebhookApi(
    @PathVariable id: String,
    @RequestBody @Valid request: WebhookApiRequest
  ): Mono<WebhookApiResponse> {
    return service.updateWebhookApi(id, request)
      .map { WebhookApiResponse(it) }
  }

  @GetMapping(
    "/topics",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun myTopics(): Flux<Topic> {
    return service.myTopics()
  }
}
