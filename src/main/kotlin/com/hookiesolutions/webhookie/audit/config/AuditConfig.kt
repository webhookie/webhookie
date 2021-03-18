package com.hookiesolutions.webhookie.audit.config

import com.hookiesolutions.webhookie.common.Constants.Queue.Headers.Companion.WH_HEADER_UNBLOCKED
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.core.MessageSelector

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/3/21 18:05
 */
@Configuration
class AuditConfig {
  @Bean
  fun unblockedMessageSelector(): MessageSelector {
    return MessageSelector {
      it.headers.contains(WH_HEADER_UNBLOCKED)
    }
  }

  @Bean
  fun originalMessageSelector(
    unblockedMessageSelector: MessageSelector
  ): MessageSelector {
    return MessageSelector {
      unblockedMessageSelector.accept(it).not()
    }
  }
}
