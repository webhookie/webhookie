package com.hookiesolutions.webhookie.subscription.web

import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails
import com.hookiesolutions.webhookie.subscription.domain.CallbackDetails
import com.hookiesolutions.webhookie.subscription.service.ProviderService
import com.hookiesolutions.webhookie.subscription.web.SubscriptionAPIDocs.Companion.REQUEST_MAPPING_PROVIDER
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/4/21 13:50
 */

@RestController
@SecurityRequirement(name = OAUTH2_SCHEME)
@RequestMapping(REQUEST_MAPPING_PROVIDER)
class ProviderController(
  private val service: ProviderService
) {
  @GetMapping(
    "/entities",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun providerEntities(): Mono<Set<String>> {
    return service.providerEntities()
  }

  @GetMapping(
    "/applications",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun entityApplications(@RequestParam entity: String): Mono<Set<ApplicationDetails>> {
    return service.providerEntityApplications(entity)
  }

  @GetMapping(
    "/callbacks",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun applicationCallbacks(@RequestParam applicationId: String): Mono<Set<CallbackDetails>> {
    return service.applicationCallbacks(applicationId)
  }
}
