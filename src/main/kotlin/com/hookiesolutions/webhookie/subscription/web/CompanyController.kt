package com.hookiesolutions.webhookie.subscription.web

import com.hookiesolutions.webhookie.config.web.OpenAPIConfig
import com.hookiesolutions.webhookie.subscription.domain.Company
import com.hookiesolutions.webhookie.subscription.service.CompanyService
import com.hookiesolutions.webhookie.subscription.service.SubscriptionService
import com.hookiesolutions.webhookie.subscription.web.CompanyController.Companion.REQUEST_MAPPING_COMPANY
import com.hookiesolutions.webhookie.subscription.service.model.CreateCompanyRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
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
@RequestMapping(REQUEST_MAPPING_COMPANY)
@Validated
class CompanyController(
  private val log: Logger,
  private val subscriptionService: SubscriptionService,
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

  @PatchMapping("/subscription/{id}/unblock", produces = [MediaType.TEXT_PLAIN_VALUE])
  fun unblockSubscription(@PathVariable id: String): Mono<String> {
    log.info("Unblocking subscription: '{}'", id)
    return subscriptionService.unblockSubscriptionBy(id)
      .map { it.id!! }
  }

  companion object {
    const val REQUEST_MAPPING_COMPANY = "/company"
  }
}