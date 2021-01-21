package com.hookiesolutions.webhookie.subscription.web

import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import com.hookiesolutions.webhookie.subscription.domain.Company
import com.hookiesolutions.webhookie.subscription.service.CompanyService
import com.hookiesolutions.webhookie.subscription.service.model.CreateCompanyRequest
import com.hookiesolutions.webhookie.subscription.web.CompanyController.Companion.REQUEST_MAPPING_COMPANY
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import javax.validation.Valid

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:34
 */
@RestController
@SecurityRequirement(name = OAUTH2_SCHEME)
@RequestMapping(REQUEST_MAPPING_COMPANY)
class CompanyController(
  private val log: Logger,
  private val companyService: CompanyService
) {
  @PostMapping("",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun createCompany(@Valid @RequestBody companyRequest: CreateCompanyRequest): Mono<Company> {
    log.info("Saving Company: '{}'", companyRequest.name)
    return companyService.createCompany(companyRequest)
  }

  companion object {
    const val REQUEST_MAPPING_COMPANY = "/company"
  }
}