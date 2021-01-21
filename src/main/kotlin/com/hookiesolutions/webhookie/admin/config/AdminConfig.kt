package com.hookiesolutions.webhookie.admin.config

import com.hookiesolutions.webhookie.admin.domain.ConsumerGroup
import com.hookiesolutions.webhookie.admin.domain.ProviderGroup
import com.hookiesolutions.webhookie.admin.service.AccessGroupService
import com.hookiesolutions.webhookie.admin.service.AccessGroupServiceDelegator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 15/1/21 12:11
 */
@Configuration
class AdminConfig {
  @Bean
  fun consumerGroupServiceDelegator(
    mongoTemplate: ReactiveMongoTemplate
  ): AccessGroupServiceDelegator<ConsumerGroup> {
    return AccessGroupServiceDelegator(AccessGroupService(mongoTemplate, ConsumerGroup::class.java))
  }

  @Bean
  fun providerGroupServiceDelegator(
    mongoTemplate: ReactiveMongoTemplate
  ): AccessGroupServiceDelegator<ProviderGroup> {
    return AccessGroupServiceDelegator(AccessGroupService(mongoTemplate, ProviderGroup::class.java))
  }
}