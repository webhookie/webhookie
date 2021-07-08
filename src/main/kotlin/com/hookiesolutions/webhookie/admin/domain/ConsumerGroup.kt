package com.hookiesolutions.webhookie.admin.domain

import com.hookiesolutions.webhookie.common.Constants.Companion.DEFAULT_CONSUMER_GROUP
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 14:08
 */

@Document(collection = "consumer_group")
@TypeAlias("consumerGroup")
data class ConsumerGroup(
  override val name: String,
  override val description: String,
  @Indexed(name = "consumer_group.iamGroupName", unique = true)
  override val iamGroupName: String,
  override val enabled: Boolean = true
) : AccessGroup() {
  companion object {
    val DEFAULT = ConsumerGroup(DEFAULT_CONSUMER_GROUP, DEFAULT_CONSUMER_GROUP, DEFAULT_CONSUMER_GROUP)
  }
}
