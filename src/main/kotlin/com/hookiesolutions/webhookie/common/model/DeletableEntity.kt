package com.hookiesolutions.webhookie.common.model

data class DeletableEntity<T: AbstractEntity>(
  val entity: T,
  val deletable: Boolean
)