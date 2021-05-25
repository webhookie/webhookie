package com.hookiesolutions.webhookie.common.web

import com.hookiesolutions.webhookie.common.config.web.OpenAPIConfig.Companion.OAUTH2_SCHEME
import com.hookiesolutions.webhookie.common.service.AdminServiceDelegate
import com.hookiesolutions.webhookie.common.web.CommonAPIDocs.Companion.REQUEST_MAPPING_USER_INFO
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/3/21 11:48
 */
@RestController
@SecurityRequirement(name = OAUTH2_SCHEME)
@RequestMapping(REQUEST_MAPPING_USER_INFO)
class UserController(
  private val adminServiceDelegate: AdminServiceDelegate,
  private val securityHandler: SecurityHandler
) {
  @GetMapping(
    "",
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun userInfo(): Mono<UserResponse> {
    return securityHandler.data()
      .switchIfEmpty(AccessDeniedException("Access Denied").toMono())
      .zipWhen { adminServiceDelegate.readAllGroups() }
      .map { UserResponse.from(it.t1, it.t2.t1, it.t2.t2) }
  }
}
