package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.audit.domain.Span.Keys.Companion.SPAN_COLLECTION_NAME
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.dto.ApplicationDetails
import com.hookiesolutions.webhookie.common.model.dto.CallbackDTO
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/3/21 15:35
 */

@Document(collection = SPAN_COLLECTION_NAME)
@TypeAlias("span")
data class Span(
  val spanId: String,
  val application: ApplicationDetails,
  val callback: CallbackDTO,
  val status: SpanStatus
  ): AbstractEntity() {
  class Keys {
    companion object {
      const val SPAN_COLLECTION_NAME = "span"
    }
  }
}
