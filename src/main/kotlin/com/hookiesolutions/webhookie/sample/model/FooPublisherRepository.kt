package com.hookiesolutions.webhookie.sample.model

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 30/11/20 16:42
 */
@Repository
interface FooPublisherRepository: ReactiveMongoRepository<FooPublisher, String>