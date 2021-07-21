package com.hookiesolutions.webhookie.audit.domain

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/3/21 18:15
 */
data class TraceSummary(
  val numberOfSpans: Int,
  val numberOfNotOKSpans: Int,
  val numberOfBlockedSpans: Int,
  val numberOfSuccess: Int,
  val workingSubscriptions: Int,
  val blockedSubscriptions: Int,
  val errorSubscriptions: Int
) {
  fun isOK(): Boolean = numberOfSpans == numberOfSuccess

  class Keys {
    companion object {
      const val KEY_NUMBER_OF_SPANS = "numberOfSpans"
      const val KEY_NUMBER_OF_SUCCESS = "numberOfSuccess"
    }
  }

  companion object {
    fun unknown() = TraceSummary(-1, -1, -1, -1, -1, -1, -1)
  }
}
