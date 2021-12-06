package com.hookiesolutions.webhookie.instance.migration

import reactor.core.publisher.Mono

interface Migration {
  val toVersion: String
  fun doMigrate(): Mono<String>

  fun migrate(): Mono<String> {
    return doMigrate()
      .map { toVersion }
  }
}
