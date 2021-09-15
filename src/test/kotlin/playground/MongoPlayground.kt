package playground

import com.hookiesolutions.webhookie.audit.domain.Span
import com.hookiesolutions.webhookie.audit.domain.SubscriptionRequest
import com.hookiesolutions.webhookie.audit.domain.SpanRetry
import com.hookiesolutions.webhookie.audit.domain.SpanSendReason
import com.hookiesolutions.webhookie.common.repository.GenericRepository
import com.hookiesolutions.webhookie.common.repository.GenericRepository.Companion.mongoField
import com.hookiesolutions.webhookie.common.repository.GenericRepository.Companion.mongoVariable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators
import org.springframework.data.mongodb.core.aggregation.SetOperation
import org.springframework.data.mongodb.core.aggregation.UnsetOperation
import java.time.Instant

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 5/3/21 13:26
 */
class MongoPlayground {

  @Test
  fun concatArrays() {
    val updatesArray = "${'$'}updates"
    val lteFilter = ArrayOperators.Filter
      .filter(updatesArray)
      .`as`("update")
      .by(ComparisonOperators.Lte.valueOf("${'$'}${'$'}update.seq").lessThanEqualToValue(22))


    val map = mapOf(
      "name" to "${'$'}name",
      "seq" to 22
    )

    val af = AddFieldsOperation.addField("uArray")
      .withValueOf(arrayOf(map))


    val gtFilter = ArrayOperators.Filter
      .filter(updatesArray)
      .`as`("update")
      .by(ComparisonOperators.Gt.valueOf("${'$'}${'$'}update.seq").greaterThanValue(22))
    val newUpdates = ArrayOperators.ConcatArrays
      .arrayOf(lteFilter)
      .concat("${'$'}uArray")
      .concat(gtFilter)
    val setUpdates = SetOperation.set("updates")
      .toValueOf(newUpdates)
    val setLastUpdate = SetOperation.set("lastUpdate")
      .toValueOf(ArrayOperators.ArrayElemAt.arrayOf(updatesArray).elementAt(-1))
    val agg = AggregationUpdate.newUpdate(
      af.build(),
      setUpdates,
      setLastUpdate,
      UnsetOperation.unset("uArray")
    )

    println(agg.toString())
    println(1)
  }

  @Test
  fun mongoAgg2() {
    val key = "tmp"

    val expr: AggregationExpression = GenericRepository.eqFilter(Span.Keys.KEY_RETRY_HISTORY, SpanRetry.KEY_RETRY_NO, 3)
    val f = AddFieldsOperation
        .addField(key)
        .withValue(expr)
        .build()
    val r = SubscriptionRequest(
      mapOf(),
      "",
      ""
    )
    val operations = arrayOf(
      f,
      GenericRepository.mongoSet("$key.response.statusCode", 405),
      GenericRepository.mongoSet(Span.Keys.KEY_RETRY_HISTORY,
      GenericRepository.insertIntoArray(Span.Keys.KEY_RETRY_HISTORY, SpanRetry.KEY_RETRY_NO, key, 3)),
      GenericRepository.mongoSet(Span.Keys.KEY_LATEST_RESULT, SpanRetry(Instant.now(), 3, 10, "", SpanSendReason.RETRY, r)),
      GenericRepository.mongoSetLastElemOfArray(Span.Keys.KEY_RETRY_HISTORY, Span.Keys.KEY_NEXT_RETRY),
      GenericRepository.mongoUnset(key)
    )

    val agg = AggregationUpdate.newUpdate(*operations)
    println(agg.toString())
    println(1)
  }

  @Test
  fun testMongoVariable() {
    println(mongoVariable("a"))
    println(mongoVariable("a.b"))
    println(mongoVariable("a.b", "c"))
    println(mongoVariable("a.b", "c", "d.e"))
    println(mongoField("a.b"))
    assertThat(mongoVariable("a")).isEqualTo("${'$'}${'$'}a")
    assertThat(mongoVariable("a.b")).isEqualTo("${'$'}${'$'}a.b")
    assertThat(mongoVariable("a", "b")).isEqualTo("${'$'}${'$'}a.b")
  }
}
