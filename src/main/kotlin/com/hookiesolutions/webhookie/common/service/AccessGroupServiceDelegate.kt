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

package com.hookiesolutions.webhookie.common.service

import com.hookiesolutions.webhookie.admin.service.AccessGroupVerifier
import com.hookiesolutions.webhookie.admin.service.ConsumerGroupService
import com.hookiesolutions.webhookie.admin.service.ProviderGroupService
import com.hookiesolutions.webhookie.security.service.SecurityHandler
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 21/1/21 14:38
 */
@Service
class AccessGroupServiceDelegate(
  private val accessGroupVerifier: AccessGroupVerifier,
  private val consumerGroupService: ConsumerGroupService,
  private val provideGroupService: ProviderGroupService,
  private val securityHandler: SecurityHandler
) {
  fun verifyGroups(
    consumerGroups: Set<String>,
    providerGroups: Set<String>,
  ): Mono<Boolean> {
    return accessGroupVerifier.verifyConsumerGroups(consumerGroups)
      .zipWhen { accessGroupVerifier.verifyProviderGroups(providerGroups) }
      .thenReturn(true)
  }

  fun extractMyValidConsumerGroups(groups: Set<String>): Mono<Set<String>> {
    return accessGroupVerifier.consumerGroupsIntersect(groups)
      .zipWhen { securityHandler.groups() }
      .map { it.t1.intersect(it.t2) }
      .filter { it.isNotEmpty() }
      .switchIfEmpty { IllegalArgumentException("At least one valid ConsumerGroup is required").toMono() }
  }

  fun readAllGroups(): Mono<Tuple2<MutableList<String>, MutableList<String>>> {
    return consumerGroupService.allGroups().map { it.iamGroupName }.collectList()
      .zipWith(provideGroupService.allGroups().map { it.iamGroupName }.collectList())
  }
}
