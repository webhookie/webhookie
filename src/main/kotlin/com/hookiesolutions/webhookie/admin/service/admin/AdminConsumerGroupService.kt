package com.hookiesolutions.webhookie.admin.service.admin

import com.hookiesolutions.webhookie.admin.domain.ConsumerGroup
import com.hookiesolutions.webhookie.admin.service.ConsumerGroupService
import com.hookiesolutions.webhookie.admin.service.model.SaveGroupRequest
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/7/21 12:48
 */
@Service
class AdminConsumerGroupService(
  override val service: ConsumerGroupService,
): AdminAccessGroupService<ConsumerGroup>(service) {
  override fun createGroup(body: SaveGroupRequest): Mono<ConsumerGroup> {
    return if(body.isSimilarToDefault()) {
      Mono.error(AccessDeniedException("Unable to create Default Consumer group"))
    } else {
      return super.createGroup(body)
    }
  }

  override fun updateGroupById(id: String, body: SaveGroupRequest): Mono<ConsumerGroup> {
    return if(body.isSimilarToDefault()) {
      Mono.error(AccessDeniedException("Unable to update Default Consumer group"))
    } else {
      return service.groupByIAM(ConsumerGroup.DEFAULT.iamGroupName)
        .flatMap {
          if(id == it.id) {
            Mono.error(AccessDeniedException("Unable to update Default Consumer group"))
          }
          else {
            super.updateGroupById(id, body)
          }
        }
    }
  }

  override fun deleteGroupById(id: String): Mono<String> {
    return service.groupByIAM(ConsumerGroup.DEFAULT.iamGroupName)
      .flatMap {
        if(id == it.id) {
          Mono.error(AccessDeniedException("Unable to update Default Consumer group"))
        }
        else {
          super.deleteGroupById(id)
        }
      }

  }

  override fun allGroups(): Flux<ConsumerGroup> {
    return super.allGroups()
      .filter { it.iamGroupName != ConsumerGroup.DEFAULT.iamGroupName }
  }
}
