package com.hookiesolutions.webhookie.subscription.web

import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.service.CompanyService
import com.hookiesolutions.webhookie.subscription.service.model.CreateApplicationRequest
import com.hookiesolutions.webhookie.subscription.web.CompanyController.Companion.REQUEST_MAPPING_COMPANY
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
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
@SecurityRequirement(name = OpenAPIConfig.OAUTH2_SCHEME)
class ApplicationController(
  private val log: Logger,
  private val companyService: CompanyService
) {
  @PostMapping(
    "$REQUEST_MAPPING_COMPANY/{companyId}$REQUEST_MAPPING_APPLICATION",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun addApplication(@PathVariable companyId: String, @Valid @RequestBody body: CreateApplicationRequest): Mono<Application> {
    log.info("adding Application to company: '{}'", companyId)
    return companyService.addApplicationTo(companyId, body)
  }

  companion object {
    const val REQUEST_MAPPING_APPLICATION = "/application"
  }
}