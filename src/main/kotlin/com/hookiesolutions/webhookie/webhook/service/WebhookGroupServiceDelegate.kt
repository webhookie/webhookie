package com.hookiesolutions.webhookie.webhook.service

import com.hookiesolutions.webhookie.security.service.SecurityHandler
import org.slf4j.Logger
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

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

  //TODO: refactor
  fun providerTopicsConsideringAdmin(): Mono<TopicsWithAccess> {
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
      .map { TopicsWithAccess(it.t2, it.t1) }
      .onErrorReturn(TopicsWithAccess.NO_ACCESS)
  }
}

//TODO: refactor
data class TopicsWithAccess(
  val topics: List<String>,
  val isAdmin: Boolean
) {
  companion object {
    val NO_ACCESS = TopicsWithAccess(emptyList(), false)
  }
}
