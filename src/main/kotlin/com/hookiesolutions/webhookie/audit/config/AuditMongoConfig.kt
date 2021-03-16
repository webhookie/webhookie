package com.hookiesolutions.webhookie.audit.config

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.domain.Trace
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/3/21 18:17
 */
@Configuration
class AuditMongoConfig {
  @Bean
  fun auditIndexEntities(): List<Class<out AbstractEntity>> =
    listOf(Trace::class.java, Span::class.java)
}
