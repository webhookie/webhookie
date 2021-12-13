/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.admin.config

import com.hookiesolutions.webhookie.admin.domain.ConsumerGroup
import com.hookiesolutions.webhookie.admin.service.ConsumerGroupService
import com.hookiesolutions.webhookie.admin.service.model.SaveGroupRequest
import com.hookiesolutions.webhookie.ingress.config.IngressProperties
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
  private val ingressProperties: IngressProperties,
  private val log: Logger
) {
  @Order(0)
  @EventListener(ApplicationReadyEvent::class)
  fun checkDefaultConsumerGroup() {
    val defaultConsumerGroup = ConsumerGroup.DEFAULT
    if(ingressProperties.addDefaultGroup) {
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
