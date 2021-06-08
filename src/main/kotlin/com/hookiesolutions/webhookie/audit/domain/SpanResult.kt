package com.hookiesolutions.webhookie.audit.domain

import com.hookiesolutions.webhookie.common.message.publisher.PublisherOtherErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherRequestErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherResponseErrorMessage
import com.hookiesolutions.webhookie.common.message.publisher.PublisherSuccessMessage
import org.springframework.http.HttpHeaders
import java.time.Instant

data class SpanResult (
  val time: Instant,
  val statusCode: Int,
  val body: String,
  val headers: HttpHeaders,
  val retryNo: Int
) {
  class Builder {
    private lateinit var time: Instant
    private var statusCode: Int = -1
    private lateinit var body: String
    private lateinit var headers: HttpHeaders
    private var retryNo: Int = -1

    fun time(time: Instant) = apply { this.time = time }

    fun message(message: PublisherSuccessMessage) = apply {
      this.statusCode = message.response.status.value()
      this.body = message.response.body()
      this.headers = message.response.headers
      this.retryNo = message.subscriptionMessage.totalNumberOfTries
    }

    fun message(message: PublisherRequestErrorMessage) = apply {
      this.statusCode = -1
      this.body = message.reason
      this.headers = message.headers
      this.retryNo = message.subscriptionMessage.totalNumberOfTries
    }

    fun message(message: PublisherOtherErrorMessage) = apply {
      this.statusCode = -1
      this.body = message.reason
      this.headers = HttpHeaders()
      this.retryNo = message.subscriptionMessage.totalNumberOfTries
    }

    fun message(message: PublisherResponseErrorMessage) = apply {
      this.statusCode = message.response.status.value()
      this.body = message.response.body()
      this.headers = message.response.headers
      this.retryNo = message.subscriptionMessage.totalNumberOfTries
    }

    fun build(): SpanResult {
      return SpanResult(time, statusCode, body, headers, retryNo)
    }
  }
}
