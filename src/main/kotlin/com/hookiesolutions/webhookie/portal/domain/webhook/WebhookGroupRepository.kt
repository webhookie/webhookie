package com.hookiesolutions.webhookie.portal.domain.webhook

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 16:39
 */
interface WebhookGroupRepository: ReactiveMongoRepository<WebhookGroup, String>