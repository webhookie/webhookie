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

package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.repository.GenericRepository
import com.hookiesolutions.webhookie.subscription.domain.Application.Queries.Companion.applicationConsumerGroupsIn
import com.hookiesolutions.webhookie.subscription.domain.Application.Queries.Companion.applicationConsumerGroupsIs
import com.hookiesolutions.webhookie.subscription.domain.Application.Queries.Companion.applicationsByEntity
import com.hookiesolutions.webhookie.subscription.domain.Application.Updates.Companion.pullConsumerGroup
import com.hookiesolutions.webhookie.subscription.domain.Application.Updates.Companion.setConsumerGroup
import com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationReadAccess
import com.hookiesolutions.webhookie.subscription.service.security.annotation.VerifyApplicationWriteAccess
import com.mongodb.client.result.UpdateResult
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 27/1/21 18:48
 */
@Repository
class ApplicationRepository(
  private val mongoTemplate: ReactiveMongoTemplate,
): GenericRepository<Application>(mongoTemplate, Application::class.java) {
  fun userApplications(entity: String, userGroups: Collection<String>): Flux<Application> {
    val criteria = applicationsByEntity(entity)
      .andOperator(applicationConsumerGroupsIn(userGroups))
    return mongoTemplate
      .find(
        query(criteria),
        Application::class.java
      )
  }

  @VerifyApplicationReadAccess
  fun findByIdVerifyingReadAccess(id: String): Mono<Application> {
    return findById(id)
  }

  @VerifyApplicationWriteAccess
  fun findByIdVerifyingWriteAccess(id: String): Mono<Application> {
    return findById(id)
  }

  fun removeConsumerGroup(value: String): Mono<UpdateResult> {
    return mongoTemplate
      .updateMulti(
        query(applicationConsumerGroupsIs(value)),
        pullConsumerGroup(value),
        Application::class.java
      )
  }

  fun updateConsumerGroup(oldValue: String, newValue: String): Mono<UpdateResult> {
    return mongoTemplate
      .updateMulti(
        query(applicationConsumerGroupsIs(oldValue)),
        setConsumerGroup(newValue),
        Application::class.java
      )
  }
}
