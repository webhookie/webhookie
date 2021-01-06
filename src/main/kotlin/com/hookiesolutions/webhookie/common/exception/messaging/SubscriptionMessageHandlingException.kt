package com.hookiesolutions.webhookie.common.exception.messaging

data class SubscriptionMessageHandlingException(
  val reason: String,
  val traceId: String? = null,
  val spanId: String? = null
): RuntimeException(reason)