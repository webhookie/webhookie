package com.hookiesolutions.webhookie.admin.service

import com.hookiesolutions.webhookie.admin.domain.AccessGroupRepository
import com.hookiesolutions.webhookie.admin.domain.ConsumerGroup
import org.slf4j.Logger
import org.springframework.stereotype.Service

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 25/1/21 09:12
 */
@Service
class ConsumerGroupService(
  override val repository: AccessGroupRepository<ConsumerGroup>,
  override val factory: AccessGroupFactory,
  override val publisher: EntityEventPublisher,
  override val log: Logger,
) : AccessGroupService<ConsumerGroup>(repository, factory, publisher, log, ConsumerGroup::class.java)