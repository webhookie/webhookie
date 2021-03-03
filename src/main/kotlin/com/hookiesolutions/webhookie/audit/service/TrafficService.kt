package com.hookiesolutions.webhookie.audit.service

import com.hookiesolutions.webhookie.audit.domain.Traffic
import com.hookiesolutions.webhookie.audit.domain.TrafficRepository
import com.hookiesolutions.webhookie.audit.domain.TrafficStatus
import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.message.ConsumerMessage
import com.hookiesolutions.webhookie.common.message.subscription.NoSubscriptionMessage
import com.hookiesolutions.webhookie.common.service.TimeMachine
import org.slf4j.Logger
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 2/3/21 19:06
 */
@Service
class TrafficService(
  private val repository: TrafficRepository,
  private val timeMachine: TimeMachine,
  private val log: Logger,
) {
  fun save(message: ConsumerMessage) {
    log.info("Saving new Consumer Message as traffic with traceId: '{}'", message.traceId)
    val traffic = Traffic.Builder()
      .message(message)
      .time(timeMachine.now())
      .build()
    saveOrFetch(traffic)
      .subscribe { log.debug("'{}' traffic was saved/fetched", it.traceId()) }
  }

  fun updateWithNoSubscription(message: NoSubscriptionMessage) {
    val traceId = message.traceId
    log.info("Updating traffic({}) with No Subscription", traceId)
    repository.updateWithNoSubscription(traceId, TrafficStatus.NO_SUBSCRIPTION, timeMachine.now())
      .subscribe { log.debug("'{}' traffic was updated to '{}'", it.traceId(), it.statusUpdate) }
  }

  private fun saveOrFetch(traffic: Traffic): Mono<Traffic> {
    val traceId = traffic.traceId()
    return repository.save(traffic)
      .doOnNext { log.info("'{}' Traffic saved successfully: '{}'", traceId, it.id) }
      .onErrorResume(EntityExistsException::class.java) {
        log.info("'{}' Traffic already exists! fetching the existing document...", traceId)
        repository.findByTraceId(traceId)
      }
  }
}
