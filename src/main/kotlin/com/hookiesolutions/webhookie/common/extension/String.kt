package com.hookiesolutions.webhookie.common.extension

fun String.isSimilarTo(another: String): Boolean {
  return this.trim().lowercase() == another.trim().lowercase()
}
