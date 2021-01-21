package com.hookiesolutions.webhookie.webhook.web

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_PROVIDER
import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import com.hookiesolutions.webhookie.webhook.config.WebhookGroupAPIDocs.Companion.REQUEST_MAPPING_WEBHOOK_GROUPS
import com.hookiesolutions.webhookie.webhook.domain.WebhookGroup
import com.hookiesolutions.webhookie.webhook.service.WebhookGroupService
import com.hookiesolutions.webhookie.webhook.service.model.WebhookGroupRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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
  @PreAuthorize("hasAuthority('$ROLE_PROVIDER')")
  @PostMapping(
    "",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun createWebhookGroup(@RequestBody @Valid request: WebhookGroupRequest): Mono<WebhookGroup> {
    return service.createWebhookGroup(request)
  }

  @GetMapping(
    "",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getWebhookGroups(): Flux<WebhookGroup> {
    return service.findProviderWebhookGroups()
  }

  @GetMapping(
    "/{id}",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun getWebhookGroup(@PathVariable id: String): Mono<WebhookGroup> {
    return service.readWebhookGroup(id)
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
  ): Mono<WebhookGroup> {
    return service.updateWebhookGroup(id, request)
  }
}