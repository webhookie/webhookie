package com.hookiesolutions.webhookie.admin.service

import com.hookiesolutions.webhookie.admin.domain.AccessGroup
import com.hookiesolutions.webhookie.admin.domain.ConsumerGroup
import com.hookiesolutions.webhookie.admin.domain.ProviderGroup
import com.hookiesolutions.webhookie.admin.service.model.SaveGroupRequest
import org.springframework.stereotype.Component

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 22/1/21 15:24
 */
@Component
class AccessGroupFactory {
  @Suppress("UNCHECKED_CAST")
  fun <T : AccessGroup> createAccessGroup(
    request: SaveGroupRequest,
    clazz: Class<T>,
  ): T {
    return if (clazz == ConsumerGroup::class.java) {
      createConsumerGroup(request) as T
    } else {
      createProviderGroup(request) as T
    }
  }

  private fun createProviderGroup(request: SaveGroupRequest): ProviderGroup =
    ProviderGroup(request.name, request.description, request.iamGroupName)

  private fun createConsumerGroup(request: SaveGroupRequest): ConsumerGroup =
    ConsumerGroup(request.name, request.description, request.iamGroupName)
}