package com.hookiesolutions.webhookie.common.repository

import com.hookiesolutions.webhookie.common.annotation.Open
import com.hookiesolutions.webhookie.common.exception.EntityExistsException
import com.hookiesolutions.webhookie.common.exception.EntityNotFoundException
import com.hookiesolutions.webhookie.common.model.AbstractEntity
import com.hookiesolutions.webhookie.common.model.AbstractEntity.Queries.Companion.byId
import com.hookiesolutions.webhookie.common.model.DeletableEntity
import com.hookiesolutions.webhookie.common.model.UpdatableEntity
import com.hookiesolutions.webhookie.common.service.security.annotation.VerifyEntityCanBeDeleted
import com.hookiesolutions.webhookie.common.service.security.annotation.VerifyEntityCanBeUpdated
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate
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
    return mongoTemplate
      .findAndModify(
        query(criteria),
        AggregationUpdate.newUpdate(*operations),
        FindAndModifyOptions.options().returnNew(true),
        clazz
      )
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

    fun mongoVariable(vararg names: String): String {
      val name = names.reduce { acc, s -> "$acc.$s" }
      return mongoField(mongoField(name))
    }

    fun mongoSet(key: String, value: Any): SetOperation {
      return SetOperation
        .set(key)
        .toValueOf(value)
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
