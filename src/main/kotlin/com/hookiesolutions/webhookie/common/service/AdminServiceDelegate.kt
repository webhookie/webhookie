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
class AdminServiceDelegate(
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
