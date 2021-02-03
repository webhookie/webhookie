package com.hookiesolutions.webhookie.common.model

data class DeletableEntity<T: AbstractEntity>(
  val entity: T,
  val deletable: Boolean
) {
  companion object {
    fun <T: AbstractEntity> deletable(entity: T): DeletableEntity<T> {
      return DeletableEntity(entity, true)
    }
  }
}