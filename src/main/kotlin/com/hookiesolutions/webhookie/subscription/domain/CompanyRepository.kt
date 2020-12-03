package com.hookiesolutions.webhookie.subscription.domain

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 3/12/20 17:33
 */
interface CompanyRepository: ReactiveMongoRepository<Company, String> 