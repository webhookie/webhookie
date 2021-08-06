/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.common.repository

import com.hookiesolutions.webhookie.common.annotation.Open
import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.common.model.DeletableEntity
import com.hookiesolutions.webhookie.common.model.UpdatableEntity
import com.hookiesolutions.webhookie.common.repository.GenericRepository.Keys.Companion.KEY_GROUP_COUNT
import com.hookiesolutions.webhookie.common.service.security.annotation.VerifyEntityCanBeDeleted
import com.hookiesolutions.webhookie.common.service.security.annotation.VerifyEntityCanBeUpdated
import com.hookiesolutions.webhookie.subscription.domain.SubscriptionRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators
import org.springframework.data.mongodb.core.aggregation.SetOperation
import org.springframework.data.mongodb.core.aggregation.UnsetOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query.query
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 6/2/21 19:24
 */
@Open
abstract class GenericRepository<E: AbstractEntity>(
  private val mongoTemplate: ReactiveMongoTemplate,
  private val clazz: Class<E>
) {
  fun save(e: E): Mono<E> {
    return mongoTemplate
      .save(e)
      .onErrorMap(DuplicateKeyException::class.java) {
        EntityExistsException(it.localizedMessage)
      }
  }

  fun findById(id: String): Mono<E> {
    return mongoTemplate
      .findById(id, clazz)
      .switchIfEmpty(EntityNotFoundException("${clazz.simpleName} not found by id: '$id'").toMono())
  }

  @VerifyEntityCanBeDeleted
  fun delete(deletableEntity: DeletableEntity<E>): Mono<String> {
    return mongoTemplate.remove(deletableEntity.entity)
      .map { deletableEntity.entity.id!! }
  }

  @VerifyEntityCanBeUpdated
  fun update(updatableEntity: UpdatableEntity<E>, id: String): Mono<E> {
    updatableEntity.entity.version = updatableEntity.entity.version?.plus(1)
    return mongoTemplate
      .update(clazz)
      .matching(query(byId(id)))
      .replaceWith(updatableEntity.entity)
      .withOptions(FindAndReplaceOptions.options().returnNew())
      .findAndReplace()
      .onErrorMap(DuplicateKeyException::class.java) {
        EntityExistsException(it.localizedMessage)
      }
  }

  fun <T: AbstractEntity> aggregationUpdate(
    criteria: Criteria,
    clazz: Class<T>,
    vararg operations: AggregationOperation
  ): Mono<T> {
    val query = query(criteria)
    return mongoTemplate
      .findAndModify(
        query,
        AggregationUpdate.newUpdate(*operations),
        FindAndModifyOptions.options().returnNew(true),
        clazz
      )
  }

  fun sort(
    aggregation: Aggregation,
    requestedPageable: Pageable,
    defaultSort: Sort,
    defaultPageable: Pageable
  ) {
    val pageable =
      Query.pageableWith(requestedPageable,
        SubscriptionRepository.SUBSCRIPTION_DEFAULT_SORT,
        SubscriptionRepository.SUBSCRIPTION_DEFAULT_PAGE
      )

    aggregation.pipeline.add(Aggregation.sort(pageable.sort))
    aggregation.pipeline.add(Aggregation.skip((pageable.pageNumber * pageable.pageSize).toLong()))
    aggregation.pipeline.add(Aggregation.limit(pageable.pageSize.toLong()))
  }

  fun exists(criteria: Criteria): Mono<Boolean> {
    return mongoTemplate.exists(query(criteria), clazz)
  }

  private fun countGroupBy(id: String, `as`: String = KEY_GROUP_COUNT): AggregationOperation {
    return Aggregation.group(mongoField(id)).count().`as`(`as`)
  }

  fun countGroupBy(vararg id: String): AggregationOperation {
    return countGroupBy(fieldName(*id))
  }

  fun projectGroupAs(name: String, count: String = KEY_GROUP_COUNT): AggregationOperation {
    return Aggregation
      .project()
      .and("_id").`as`(name)
      .andInclude(KEY_GROUP_COUNT)
      .andExclude("_id")
  }

  class Query {
    companion object {
      fun pageableWith(requested: Pageable, defaultSort: Sort, defaultPageable: Pageable): Pageable {
        val page = if(requested.isPaged) {
          requested.pageNumber
        } else {
          defaultPageable.pageNumber
        }

        val size = if(requested.isPaged) {
          requested.pageSize
        } else {
          defaultPageable.pageSize
        }

        val sort = requested.getSortOr(defaultSort)

        return PageRequest.of(page, size, sort)
      }
    }
  }

  class Keys {
    companion object {
      const val KEY_GROUP_COUNT = "count"
    }
  }

  companion object {
    fun addMongoObjectToArrayField(key: String, obj: Any): AddFieldsOperation {
      return AddFieldsOperation
        .addField(key)
        .withValueOf(arrayOf(obj))
        .build()
    }

    fun addMongoField(key: String, expr: AggregationExpression): AddFieldsOperation {
      return AddFieldsOperation
        .addField(key)
        .withValue(expr)
        .build()
    }

    fun mongoField(name: String): String {
      return "${'$'}$name"
    }

    fun fieldName(vararg names: String): String {
      return names.reduce { acc, s -> "$acc.$s" }
    }

    fun mongoVariable(vararg names: String): String {
      val name = fieldName(*names)
      return mongoField(mongoField(name))
    }

    fun mongoSet(key: String, value: Any): SetOperation {
      return SetOperation
        .set(key)
        .toValueOf(value)
    }

    fun mongoIncOperation(key: String, value: Int = 1): AddFieldsOperation {
      return AddFieldsOperation
        .addField(key)
        .withValue(ArithmeticOperators.Add.valueOf(mongoField(key)).add(value))
        .build()
    }

    fun mongoSetLastElemOfArray(arrayField: String, key: String): SetOperation {
      return SetOperation.set(key)
        .toValueOf(ArrayOperators.ArrayElemAt.arrayOf(mongoField(arrayField)).elementAt(-1))
    }

    fun mongoUnset(vararg keys: String): UnsetOperation {
      return UnsetOperation
        .unset(*keys)
    }

    fun insertIntoArray(arrayField: String, fieldPath: String, tempKey: String, value: Any): AggregationExpression {
      return ArrayOperators.ConcatArrays
        .arrayOf(ltFilter(arrayField, fieldPath, value))
        .concat(mongoField(tempKey))
        .concat(gtFilter(arrayField, fieldPath, value))
    }

    fun concatArrays(arrayField: String, tempKey: String): AggregationExpression {
      return ArrayOperators.ConcatArrays
        .arrayOf(mongoField(arrayField))
        .concat(mongoField(tempKey))
    }

    fun conditionalValue(
      cond: AggregationExpression,
      thenValue: AggregationExpression,
      elseValue: AggregationExpression
    ): AggregationExpression {
      return ConditionalOperators.Cond
        .`when`(cond)
        .thenValueOf(thenValue)
        .otherwiseValueOf(elseValue)
    }

    fun neExpression(key: String, value: String): AggregationExpression {
      return ComparisonOperators.Ne.valueOf(mongoField(key)).notEqualTo(mongoField(value))
    }

    private fun ltFilter(fieldName: String, fieldPath: String, value: Any): AggregationExpression {
      val asName = "r"
      val expr = ComparisonOperators.Lt
        .valueOf(mongoVariable(asName, fieldPath))
        .lessThanValue(value)
      return filterBy(fieldName, asName, expr)
    }

    private fun gtFilter(fieldName: String, fieldPath: String, value: Any): AggregationExpression {
      val asName = "r"
      val expr = ComparisonOperators.Gt
        .valueOf(mongoVariable(asName, fieldPath))
        .greaterThanValue(value)
      return filterBy(fieldName, asName, expr)
    }

    fun eqFilter(fieldName: String, fieldPath: String, value: Any): AggregationExpression {
      val asName = "rhItem"
      val expr = ComparisonOperators.Eq
        .valueOf(mongoVariable(asName, fieldPath))
        .equalToValue(value)
      return filterBy(fieldName, asName, expr)
    }

    private fun filterBy(fieldName: String, asName: String, expression: AggregationExpression): AggregationExpression {
      return ArrayOperators.Filter
        .filter(mongoField(fieldName))
        .`as`(asName)
        .by(expression)
    }
  }
}
