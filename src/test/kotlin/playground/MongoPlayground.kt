package playground

import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators
import org.springframework.data.mongodb.core.aggregation.SetOperation
import org.springframework.data.mongodb.core.aggregation.UnsetOperation

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
}
