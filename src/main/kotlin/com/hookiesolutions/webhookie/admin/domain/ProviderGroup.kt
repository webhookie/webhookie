package com.hookiesolutions.webhookie.admin.domain

import com.hookiesolutions.webhookie.common.model.AbstractEntity
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 13/1/21 14:08
 */

@Document(collection = "provider_group")
@TypeAlias("providerGroup")
data class ProviderGroup(
  override val name: String,
  override val description: String,
  @Indexed(unique = true)
  override val iamGroupName: String,
  override val enabled: Boolean = true
) : AccessGroup, AbstractEntity()
