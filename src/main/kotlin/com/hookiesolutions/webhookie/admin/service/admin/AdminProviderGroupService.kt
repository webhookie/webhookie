package com.hookiesolutions.webhookie.admin.service.admin

import com.hookiesolutions.webhookie.admin.domain.ProviderGroup
import com.hookiesolutions.webhookie.admin.service.ProviderGroupService
import org.springframework.stereotype.Service

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/7/21 12:48
 */
@Service
class AdminProviderGroupService(
  override val service: ProviderGroupService,
): AdminAccessGroupService<ProviderGroup>(service)
