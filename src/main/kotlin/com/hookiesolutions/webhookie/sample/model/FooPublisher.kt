package com.hookiesolutions.webhookie.sample.model

import com.hookiesolutions.webhookie.model.AbstractEntity
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 30/11/20 16:37
 */
@Document
@TypeAlias("fooPublisher")
data class FooPublisher(
  @Indexed(unique = true)
  val name: String,

  @Indexed
  val path: String,

  val enabled: Boolean,

  val mediaType: String
): AbstractEntity()
