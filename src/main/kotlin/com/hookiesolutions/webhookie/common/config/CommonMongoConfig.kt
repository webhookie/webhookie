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

package com.hookiesolutions.webhookie.common.config

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.subscription.domain.Callback
import org.slf4j.Logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver
import org.springframework.data.mongodb.core.indexOps
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 1/3/21 18:41
 */
@Configuration
class CommonMongoConfig(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val mongoMappingContext: MongoMappingContext,
  private val logger: Logger,
  private val indexEntityList: List<List<Class<out AbstractEntity>>>
) {
  @Order(Ordered.HIGHEST_PRECEDENCE)
  @EventListener(ApplicationReadyEvent::class)
  fun deleteCallbackAfterStartup() {
    mongoTemplate.indexOps<Callback>()
      .dropIndex("callback_request_target")
      .subscribe(
        { logger.info("'callback_request_target' index was removed successfully!") },
        { logger.warn("Unable to remove 'callback_request_target' index . original message: '{}'", it.localizedMessage)}
      )
  }

  @Order(Ordered.HIGHEST_PRECEDENCE + 1)
  @EventListener(ApplicationReadyEvent::class)
  fun initIndicesAfterStartup() {
    val resolver = MongoPersistentEntityIndexResolver(mongoMappingContext)

    indexEntityList.flatten()
      .toFlux()
      .flatMap { clazz ->
        resolver
          .resolveIndexFor(clazz)
          .toFlux()
          .flatMap { Mono.zip(mongoTemplate.indexOps(clazz).toMono(), it.toMono()) }
      }
      .flatMap {
        val indexOps = it.t1
        val def = it.t2
        indexOps.ensureIndex(def)
          .zipWith(def.toMono())
      }
      .subscribe { logger.info("ensureIndex: '{}', '{}', '{}'", it.t1, it.t2.indexKeys, it.t2.indexOptions) }
  }
}
