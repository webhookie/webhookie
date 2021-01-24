package com.hookiesolutions.webhookie.admin.domain

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Repository

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 25/1/21 09:09
 */
@Repository
class ConsumerGroupRepository(
  override val mongoTemplate: ReactiveMongoTemplate,
) : AccessGroupRepository<ConsumerGroup>(mongoTemplate, ConsumerGroup::class.java)