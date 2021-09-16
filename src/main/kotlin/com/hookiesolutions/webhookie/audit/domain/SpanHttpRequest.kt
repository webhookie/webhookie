package com.hookiesolutions.webhookie.audit.domain

data class SpanHttpRequest(
  val headers: Map<String, Any>,
  val contentType: String,
  val payload: String
)
