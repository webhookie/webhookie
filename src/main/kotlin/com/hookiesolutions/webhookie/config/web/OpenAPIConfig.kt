package com.hookiesolutions.webhookie.config.web

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:33
 */
@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "webHookie API",
        version = "1.0.0"
    )
)
@SecurityScheme(
    name = OpenAPIConfig.BASIC_SCHEME,
    type = SecuritySchemeType.HTTP,
    scheme = "basic"
)
class OpenAPIConfig {
  companion object {
    const val BASIC_SCHEME = "basicScheme"
  }
}