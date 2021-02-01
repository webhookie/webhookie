package com.hookiesolutions.webhookie.common.service

import com.hookiesolutions.webhookie.admin.service.AccessGroupVerifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 21/1/21 14:38
 */
@Service
class AdminServiceDelegate(
  private val accessGroupVerifier: AccessGroupVerifier
) {
  fun verifyGroups(
    consumerGroups: Set<String>,
    providerGroups: Set<String>,
  ): Mono<Boolean> {
    return accessGroupVerifier.verifyConsumerGroups(consumerGroups)
      .zipWhen { accessGroupVerifier.verifyProviderGroups(providerGroups) }
      .thenReturn(true)
  }

  fun verifyConsumerGroups(groups: Set<String>): Mono<Boolean> {
    return accessGroupVerifier.verifyConsumerGroups(groups)
      .thenReturn(true)
  }
}