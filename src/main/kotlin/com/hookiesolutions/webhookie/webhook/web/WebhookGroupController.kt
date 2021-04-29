package com.hookiesolutions.webhookie.webhook.web

import com.fasterxml.jackson.annotation.JsonView
import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import com.hookiesolutions.webhookie.webhook.config.WebhookGroupAPIDocs.Companion.REQUEST_MAPPING_WEBHOOK_GROUPS
import com.hookiesolutions.webhookie.webhook.domain.Topic
import com.hookiesolutions.webhookie.webhook.service.WebhookGroupService
import com.hookiesolutions.webhookie.webhook.service.model.WebhookGroupRequest
import com.hookiesolutions.webhookie.webhook.web.response.WebhookGroupResponse
import com.hookiesolutions.webhookie.webhook.web.response.WebhookGroupViews
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
@RequestMapping(REQUEST_MAPPING_WEBHOOK_GROUPS)
@SecurityRequirement(name = OAUTH2_SCHEME)
class WebhookGroupController(
  private val service: WebhookGroupService
) {
  @PostMapping(
    "",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  @ResponseStatus(HttpStatus.CREATED)
  fun createWebhookGroup(@RequestBody @Valid request: WebhookGroupRequest): Mono<WebhookGroupResponse> {
    return service.createWebhookGroup(request)
      .map { WebhookGroupResponse(it) }
  }

  @GetMapping(
    "",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  @JsonView(WebhookGroupViews.Full::class)
  fun getWebhookGroups(pageable: Pageable): Flux<WebhookGroupResponse> {
    return service.findMyWebhookGroups(pageable)
      .map { WebhookGroupResponse(it) }
  }

  @GetMapping(
    "/summary",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  @JsonView(WebhookGroupViews.Summary::class)
  fun getWebhookGroupsSummary(pageable: Pageable): Flux<WebhookGroupResponse> {
    return service.findMyWebhookGroups(pageable)
      .map { WebhookGroupResponse(it) }
  }

  @GetMapping(
    "/{id}",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getWebhookGroup(@PathVariable id: String): Mono<WebhookGroupResponse> {
    return service.readWebhookGroup(id)
      .map { WebhookGroupResponse(it) }
  }

  @GetMapping(
    "/byTopic",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getWebhookGroupByTopic(@RequestParam topic: String): Mono<WebhookGroupResponse> {
    return service.readWebhookGroupByTopic(topic)
      .map { WebhookGroupResponse(it) }
  }

  @DeleteMapping(
    "/{id}",
    produces = [MediaType.TEXT_PLAIN_VALUE]
  )
  fun deleteWebhookGroup(@PathVariable id: String): Mono<String> {
    return service.deleteWebhookGroup(id)
  }

  @PutMapping(
    "/{id}",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun updateWebhookGroup(
    @PathVariable id: String,
    @RequestBody @Valid request: WebhookGroupRequest
  ): Mono<WebhookGroupResponse> {
    return service.updateWebhookGroup(id, request)
      .map { WebhookGroupResponse(it) }
  }

  @GetMapping(
    "/topics",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun myTopics(): Flux<Topic> {
    return service.myTopics()
  }
}
