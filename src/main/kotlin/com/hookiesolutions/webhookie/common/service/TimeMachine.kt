package com.hookiesolutions.webhookie.common.service

import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:38
 */
@Service
class TimeMachine {
  private fun clock(): Clock = Clock.systemUTC()

  fun now(): Instant = Instant.now(clock())

  @Suppress("unused")
  fun currentOffsetAt(timeZone: ZoneId): ZoneOffset {
    return now().atZone(timeZone).offset
  }
}