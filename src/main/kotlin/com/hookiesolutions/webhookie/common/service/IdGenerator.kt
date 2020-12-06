package com.hookiesolutions.webhookie.common.service

import org.bson.types.ObjectId
import org.springframework.stereotype.Component

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 7/12/20 00:39
 */
@Component
class IdGenerator {
  fun generate(): String {
    return ObjectId.get().toHexString()
  }
}