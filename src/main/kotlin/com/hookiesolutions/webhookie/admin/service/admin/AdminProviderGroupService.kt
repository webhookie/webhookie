package com.hookiesolutions.webhookie.admin.service.admin

import com.hookiesolutions.webhookie.admin.domain.AccessGroupRepository
import com.hookiesolutions.webhookie.admin.domain.ProviderGroup
import com.hookiesolutions.webhookie.admin.service.AccessGroupFactory
import com.hookiesolutions.webhookie.admin.service.EntityEventPublisher
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import org.slf4j.Logger
import org.springframework.stereotype.Service

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/7/21 12:48
 */
@Service
class AdminProviderGroupService(
  override val repository: AccessGroupRepository<ProviderGroup>,
  override val factory: AccessGroupFactory,
  override val publisher: EntityEventPublisher,
  override val securityHandler: SecurityHandler,
  override val log: Logger,
): AdminAccessGroupService<ProviderGroup>(
  repository, factory, publisher, securityHandler, log, ProviderGroup::class.java
)
