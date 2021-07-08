package com.hookiesolutions.webhookie.admin.config

import com.hookiesolutions.webhookie.admin.domain.ConsumerGroup
import com.hookiesolutions.webhookie.admin.service.ConsumerGroupService
import com.hookiesolutions.webhookie.admin.service.model.SaveGroupRequest
import com.hookiesolutions.webhookie.consumer.config.ConsumerProperties
import org.slf4j.Logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 8/7/21 12:16
 */
@Configuration
class DefaultConsumerGroupConfiguration(
  private val consumerGroupService: ConsumerGroupService,
  private val consumerProperties: ConsumerProperties,
  private val log: Logger
) {
  @Order(0)
  @EventListener(ApplicationReadyEvent::class)
  fun checkDefaultConsumerGroup() {
    val defaultConsumerGroup = ConsumerGroup.DEFAULT
    if(consumerProperties.addDefaultGroup) {
      log.info("checking db for default consumer group.....")
      val saveGroupRequest = SaveGroupRequest(defaultConsumerGroup.name, defaultConsumerGroup.description, defaultConsumerGroup.iamGroupName)
      consumerGroupService.groupByIAM(defaultConsumerGroup.iamGroupName)
        .switchIfEmpty(consumerGroupService.createGroup(saveGroupRequest))
        .subscribe { log.info("default consumer group is ready with id: '{}'", it.id) }
    } else {
      log.info("deleting default consumer group.....")
      consumerGroupService.groupByIAM(ConsumerGroup.DEFAULT.iamGroupName)
        .zipWhen { consumerGroupService.deleteGroupById(it.id!!) }
        .subscribe {
          log.info("Default Consumer Group with id: '{}' was deleted successfully!", it.t1.id)
        }
    }
  }
}
