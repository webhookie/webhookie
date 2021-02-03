package com.hookiesolutions.webhookie.common.model

data class UpdatableEntity<T: AbstractEntity>(
  val entity: T,
  val updatable: Boolean
) {
  companion object {
    @JvmStatic
    fun <T: AbstractEntity> updatable(entity: T): UpdatableEntity<T> {
      return UpdatableEntity(entity, true)
    }
  }
}