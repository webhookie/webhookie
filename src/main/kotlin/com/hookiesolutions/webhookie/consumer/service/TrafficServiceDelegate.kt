package com.hookiesolutions.webhookie.consumer.service

import com.hookiesolutions.webhookie.audit.service.TraceService
import org.slf4j.Logger
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 12/7/21 16:22
 */
@Service
class TrafficServiceDelegate(
  private val log: Logger,
  private val traceService: TraceService
) {
  fun traceIdExists(traceId: String): Mono<Boolean> {
    log.info("checking trace id existence '{}'", traceId)
    return traceService.traceIdExists(traceId)
  }
}
