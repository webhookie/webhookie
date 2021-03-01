package com.hookiesolutions.webhookie.admin.config

import com.hookiesolutions.webhookie.admin.domain.ConsumerGroup
import com.hookiesolutions.webhookie.admin.domain.ProviderGroup
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 14:10
 */
@Configuration
class AdminMongoConfig {
  @Bean
  fun adminIndexEntities(): List<Class<out AbstractEntity>> =
    listOf(
      ConsumerGroup::class.java,
      ProviderGroup::class.java
    )
}
