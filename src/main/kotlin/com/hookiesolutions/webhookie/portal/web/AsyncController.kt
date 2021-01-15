package com.hookiesolutions.webhookie.portal.web

import amf.client.model.document.Document
import com.hookiesolutions.webhookie.common.Constants.Security.Roles.Companion.ROLE_CONSUMER
import com.hookiesolutions.webhookie.portal.web.io.AsyncDocumentReader.Companion.TEXT_YAML_VALUE
import com.hookiesolutions.webhookie.portal.service.model.WebhookGroup
import com.hookiesolutions.webhookie.portal.service.AsyncApiService
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class AsyncController(
  private val service: AsyncApiService
) {
  @PreAuthorize("hasAuthority('$ROLE_CONSUMER')")
  @PostMapping("/async/read",
    consumes = [TEXT_YAML_VALUE, APPLICATION_JSON_VALUE],
    produces = [APPLICATION_JSON_VALUE]
  )
  fun read(@RequestBody body: Document): Mono<WebhookGroup> {
    return service.read(body)
  }
}