package com.hookiesolutions.webhookie.portal.web

import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_PROVIDER
import com.hookiesolutions.webhookie.portal.domain.webhook.WebhookGroup
import com.hookiesolutions.webhookie.portal.service.WebhookService
import com.hookiesolutions.webhookie.portal.service.model.WebhookGroupRequest
import com.hookiesolutions.webhookie.portal.web.PortalAPIDocs.Companion.REQUEST_MAPPING_PORTAL_ADMIN
import com.hookiesolutions.webhookie.portal.web.WebhookGroupsController.Companion.REQUEST_MAPPING_WEBHOOK_GROUPS
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import javax.validation.Valid

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 15:16
 */
@RestController
@RequestMapping("$REQUEST_MAPPING_PORTAL_ADMIN$REQUEST_MAPPING_WEBHOOK_GROUPS")
class WebhookGroupsController(
  private val service: WebhookService
) {
  @PreAuthorize("hasAuthority('$ROLE_PROVIDER')")
  @PostMapping(
    "",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun createWebhookGroup(@RequestBody @Valid request: WebhookGroupRequest): Mono<WebhookGroup> {
    return service.createGroup(request)
  }

  companion object {
    const val REQUEST_MAPPING_WEBHOOK_GROUPS = "/webhookgroups"
  }
}