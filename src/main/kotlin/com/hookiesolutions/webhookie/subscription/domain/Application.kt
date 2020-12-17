package com.hookiesolutions.webhookie.subscription.domain

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 16:26
 */
@Document(collection = "application")
data class Application(
  val name: String,
  @Indexed
  val companyId: String
): AbstractEntity()
