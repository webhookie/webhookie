package com.hookiesolutions.webhookie.common.model

data class UpdatableEntity<T: AbstractEntity>(
  val entity: T,
  val updatable: Boolean
)