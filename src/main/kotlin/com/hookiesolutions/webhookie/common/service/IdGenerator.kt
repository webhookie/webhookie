package com.hookiesolutions.webhookie.common.service

import org.springframework.stereotype.Component
import java.util.UUID

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 7/12/20 00:39
 */
@Component
class IdGenerator {
  fun generate(): String {
    return UUID.randomUUID().toString()
  }
}
