package com.hookiesolutions.webhookie.webhook.service

import com.hookiesolutions.webhookie.security.service.SecurityHandler
import org.slf4j.Logger
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 10/2/21 19:13
 */
@Service
class WebhookGroupServiceDelegate(
  private val webhookGroupService: WebhookGroupService,
  private val securityHandler: SecurityHandler,
  private val log: Logger,
) {
  fun providerTopics(): Mono<List<String>> {
    return webhookGroupService.myTopics()
      .map { it.name }
      .collectList()
  }

  fun providerTopicsConsideringAdmin(): Mono<Tuple2<Boolean, List<String>>> {
    return securityHandler.data()
      .map { it.hasAdminAuthority() }
      .zipWhen { isAdmin ->
        return@zipWhen if(isAdmin) {
          log.info("Fetching all traces form ADMIN")
          emptyList<String>().toMono()
        } else {
          providerTopics()
        }
      }
      .onErrorReturn(Tuples.of(false, emptyList()))
  }
}
