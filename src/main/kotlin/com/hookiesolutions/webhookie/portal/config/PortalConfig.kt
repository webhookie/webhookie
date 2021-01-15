package com.hookiesolutions.webhookie.portal.config

import com.hookiesolutions.webhookie.portal.domain.ConsumerGroup
import com.hookiesolutions.webhookie.portal.domain.ProviderGroup
import com.hookiesolutions.webhookie.portal.service.AccessGroupService
import com.hookiesolutions.webhookie.portal.service.AccessGroupServiceDelegator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 15/1/21 12:11
 */
@Configuration
class PortalConfig {
  @Bean
  fun consumerGroupServiceDelegator(
    accessGroupService: AccessGroupService
  ): AccessGroupServiceDelegator<ConsumerGroup> {
    return AccessGroupServiceDelegator(accessGroupService, ConsumerGroup::class.java)
  }

  @Bean
  fun providerGroupServiceDelegator(
    accessGroupService: AccessGroupService
  ): AccessGroupServiceDelegator<ProviderGroup> {
    return AccessGroupServiceDelegator(accessGroupService, ProviderGroup::class.java)
  }
}