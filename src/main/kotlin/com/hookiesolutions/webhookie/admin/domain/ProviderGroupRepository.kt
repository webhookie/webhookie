package com.hookiesolutions.webhookie.admin.domain

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Repository

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 25/1/21 09:10
 */
@Repository
class ProviderGroupRepository(
  override val mongoTemplate: ReactiveMongoTemplate,
) : AccessGroupRepository<ProviderGroup>(mongoTemplate, ProviderGroup::class.java)