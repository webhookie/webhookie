package com.hookiesolutions.webhookie.admin.service

import com.hookiesolutions.webhookie.admin.domain.AccessGroupRepository
import com.hookiesolutions.webhookie.admin.domain.ProviderGroup
import org.slf4j.Logger
import org.springframework.stereotype.Service

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 25/1/21 09:13
 */
@Service
class ProviderGroupService(
  override val repository: AccessGroupRepository<ProviderGroup>,
  override val factory: AccessGroupFactory,
  override val publisher: EntityEventPublisher,
  override val log: Logger,
) : AccessGroupService<ProviderGroup>(repository, factory, publisher, log, ProviderGroup::class.java)