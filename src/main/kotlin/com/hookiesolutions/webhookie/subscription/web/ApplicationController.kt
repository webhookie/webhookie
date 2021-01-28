package com.hookiesolutions.webhookie.subscription.web

import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.service.ApplicationService
import com.hookiesolutions.webhookie.subscription.service.model.CreateApplicationRequest
import com.hookiesolutions.webhookie.subscription.web.SubscriptionAPIDocs.Companion.REQUEST_MAPPING_APPLICATIONS
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import javax.validation.Valid

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 18:18
 */
@RestController
@SecurityRequirement(name = OAUTH2_SCHEME)
class ApplicationController(
  private val log: Logger,
  private val applicationService: ApplicationService
) {
  @PostMapping(
    REQUEST_MAPPING_APPLICATIONS,
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun createApplication(@Valid @RequestBody body: CreateApplicationRequest): Mono<Application> {
    log.info("adding Application: '{}'", body.name)
    return applicationService.createApplication(body)
  }
}