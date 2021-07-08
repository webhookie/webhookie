package com.hookiesolutions.webhookie.admin.web

import com.hookiesolutions.webhookie.admin.domain.ConsumerGroup
import com.hookiesolutions.webhookie.admin.domain.ProviderGroup
import com.hookiesolutions.webhookie.admin.service.ConsumerGroupService
import com.hookiesolutions.webhookie.admin.service.ProviderGroupService
import com.hookiesolutions.webhookie.admin.web.GroupAPIDocs.Companion.REQUEST_MAPPING_GROUP
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/7/21 11:35
 */
@RestController
@RequestMapping(REQUEST_MAPPING_GROUP)
class AccessGroupController(
  private val consumerGroupService: ConsumerGroupService,
  private val providerGroupService: ProviderGroupService
) {
  @GetMapping(REQUEST_MAPPING_CONSUMER_GROUPS)
  fun allConsumerGroups(): Flux<ConsumerGroup> {
    return consumerGroupService.allGroups()
  }

  @GetMapping(REQUEST_MAPPING_PROVIDER_GROUPS)
  fun allProviderGroups(): Flux<ProviderGroup> {
    return providerGroupService.allGroups()
  }

  companion object {
    const val REQUEST_MAPPING_CONSUMER_GROUPS = "/consumergroups"
    const val REQUEST_MAPPING_PROVIDER_GROUPS = "/providergroups"
  }
}
