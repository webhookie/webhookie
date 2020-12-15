package com.hookiesolutions.webhookie.common.model.dto

import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 15/12/20 11:21
 */
data class BlockedDetailsDTO(
  val reason: String,
  val time: Instant
)

