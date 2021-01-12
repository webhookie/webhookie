package com.hookiesolutions.webhookie.portal

import amf.client.model.document.Document
import com.hookiesolutions.webhookie.portal.model.WebhookGroup
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class AsyncController(
  private val service: AsyncApiService
) {
  @PreAuthorize("hasAuthority('WH_CONSUMER')")
  @PostMapping("/async/read", consumes = ["text/yaml"], produces = ["application/json"])
  fun read(@RequestBody body: Document): Mono<WebhookGroup> {
    return service.read(body)
  }
}