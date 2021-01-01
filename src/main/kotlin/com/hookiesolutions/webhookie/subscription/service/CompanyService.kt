package com.hookiesolutions.webhookie.subscription.service

import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.subscription.domain.Application
import com.hookiesolutions.webhookie.subscription.domain.Company
import com.hookiesolutions.webhookie.subscription.service.model.CreateApplicationRequest
import com.hookiesolutions.webhookie.subscription.service.model.CreateCompanyRequest
import org.slf4j.Logger
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 17/12/20 22:42
 */
@Service
class CompanyService(
  private val log: Logger,
  private val factory: ConversionsFactory,
  private val mongoTemplate: ReactiveMongoTemplate
) {
  fun addApplicationTo(companyId: String, body: CreateApplicationRequest): Mono<Application> {
    return mongoTemplate.findById(companyId, Company::class.java)
      .switchIfEmpty(EntityNotFoundException("Company not found by id: '$companyId'").toMono())
      .map { factory.createApplicationRequestToApplication(body, it) }
      .flatMap { mongoTemplate.save(it) }
      .doOnNext { log.info("Application '{}' was created successfully", it.name) }
  }

  fun createCompany(companyRequest: CreateCompanyRequest): Mono<Company> {
    return mongoTemplate.save(companyRequest.company())
      .doOnNext { log.info("Company '{}' was created successfully", it.name) }
  }
}