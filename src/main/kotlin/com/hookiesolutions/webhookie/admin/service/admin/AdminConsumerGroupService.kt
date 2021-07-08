package com.hookiesolutions.webhookie.admin.service.admin

import com.hookiesolutions.webhookie.admin.domain.ConsumerGroup
import com.hookiesolutions.webhookie.admin.service.ConsumerGroupService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/7/21 12:48
 */
@Service
class AdminConsumerGroupService(
  override val service: ConsumerGroupService,
): AdminAccessGroupService<ConsumerGroup>(service) {
  override fun allGroups(): Flux<ConsumerGroup> {
    return super.allGroups()
      .filter { it.iamGroupName != ConsumerGroup.DEFAULT.iamGroupName }
  }
}
