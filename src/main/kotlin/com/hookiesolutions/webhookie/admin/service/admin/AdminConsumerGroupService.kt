package com.hookiesolutions.webhookie.admin.service.admin

import com.hookiesolutions.webhookie.admin.domain.AccessGroupRepository
import com.hookiesolutions.webhookie.admin.domain.ConsumerGroup
import com.hookiesolutions.webhookie.admin.service.AccessGroupFactory
import com.hookiesolutions.webhookie.admin.service.EntityEventPublisher
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import org.slf4j.Logger
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/7/21 12:48
 */
@Service
class AdminConsumerGroupService(
  override val repository: AccessGroupRepository<ConsumerGroup>,
  override val factory: AccessGroupFactory,
  override val publisher: EntityEventPublisher,
  override val securityHandler: SecurityHandler,
  override val log: Logger,
): AdminAccessGroupService<ConsumerGroup>(
  repository, factory, publisher, securityHandler, log, ConsumerGroup::class.java
) {
  override fun allGroups(): Flux<ConsumerGroup> {
    return super.allGroups()
      .filter { it.iamGroupName != ConsumerGroup.DEFAULT.iamGroupName }
  }
}
