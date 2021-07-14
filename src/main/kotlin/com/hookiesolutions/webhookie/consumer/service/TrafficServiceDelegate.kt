package com.hookiesolutions.webhookie.consumer.service

import com.hookiesolutions.webhookie.audit.service.TraceService
import com.hookiesolutions.webhookie.common.service.IdGenerator
import org.slf4j.Logger
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 12/7/21 16:22
 */
@Service
class TrafficServiceDelegate(
  private val log: Logger,
  private val idGenerator: IdGenerator,
  private val traceService: TraceService
) {
  fun traceIdExists(traceId: String): Mono<String> {
    log.info("checking trace id existence '{}'", traceId)
    return traceService.traceIdExists(traceId)
      .flatMap {
        return@flatMap if (it) {
          Mono.error(DuplicateKeyException("TraceId $traceId already exists!"))
        } else {
          traceId.toMono()
        }
      }
  }

  fun checkOrGenerateTrace(traceId: String?): Mono<String> {
    return if (StringUtils.hasText(traceId)) {
      traceIdExists(traceId!!)
    } else {
      idGenerator.generate().toMono()
    }
  }
}
