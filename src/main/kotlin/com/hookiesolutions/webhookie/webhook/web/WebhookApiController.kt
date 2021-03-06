/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

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
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.CollectionUtils
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
    "/{id}/spec",
    produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
  )
  fun downloadSpec(@PathVariable id: String): Mono<ResponseEntity<Resource>> {
    return service.readWebhookApi(id)
      .map {
        val resource = ByteArrayResource(it.raw.toByteArray())
        val headers = CollectionUtils.unmodifiableMultiValueMap(
          CollectionUtils.toMultiValueMap(
            mapOf(
              HttpHeaders.CONTENT_DISPOSITION to listOf("attachment; ${it.title}.yml"),
              "Cache-Control" to listOf("no-cache, no-store, must-revalidate"),
              "Pragma" to listOf("no-cache"),
              "Expires" to listOf("0")
            )
          )
        )
        ResponseEntity.ok()
          .headers(HttpHeaders(headers))
          .contentLength(it.raw.length.toLong())
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .body(resource)
      }
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
