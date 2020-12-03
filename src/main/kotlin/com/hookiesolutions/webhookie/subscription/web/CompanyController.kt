package com.hookiesolutions.webhookie.subscription.web

import com.hookiesolutions.webhookie.config.OpenAPIConfig
import com.hookiesolutions.webhookie.subscription.domain.Company
import com.hookiesolutions.webhookie.subscription.domain.CompanyRepository
import com.hookiesolutions.webhookie.subscription.web.CompanyController.Companion.COMPANY_REQUEST_MAPPING
import com.hookiesolutions.webhookie.subscription.web.model.CreateCompanyRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
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
@SecurityRequirement(name = OpenAPIConfig.BASIC_SCHEME)
@RequestMapping(COMPANY_REQUEST_MAPPING)
@Validated
class CompanyController(
  private val log: Logger,
  private val companyRepository: CompanyRepository
) {
  @PostMapping("",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun createCompany(@Valid @RequestBody companyRequest: CreateCompanyRequest): Mono<Company> {
    log.info("Saving Company: '{}', '{}' subscriptions", companyRequest.name, companyRequest.subscriptions.size)
    return companyRepository.save(companyRequest.company())
  }

  companion object {
    const val COMPANY_REQUEST_MAPPING = "/company"
  }
}