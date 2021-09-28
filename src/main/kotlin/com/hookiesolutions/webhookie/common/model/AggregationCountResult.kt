package com.hookiesolutions.webhookie.common.model

data class AggregationCountResult(
  val count: Long
) {
  companion object {
    val ZERO = AggregationCountResult(0)
  }
}
