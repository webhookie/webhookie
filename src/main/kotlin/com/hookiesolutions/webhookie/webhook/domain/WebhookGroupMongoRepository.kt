package com.hookiesolutions.webhookie.webhook.domain

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 19/1/21 16:39
 */
interface WebhookGroupMongoRepository: ReactiveMongoRepository<WebhookGroup, String>