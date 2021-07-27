/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

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
