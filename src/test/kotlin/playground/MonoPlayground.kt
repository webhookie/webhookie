package playground

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 9/2/21 10:06
 */
class MonoPlayground {
  @Test
  fun testOptional() {
    val str: String? = null

    Mono
      .create<String> {
        it.success(str)
      }
      .switchIfEmpty("Replaced".toMono())
      .subscribe {
        println(it)
      }

    println(2)
  }

  @Test
  fun om() {
    val om = ObjectMapper()
    Mono
      .create<String> {
        it.success(om.writeValueAsString(null))
      }
      .switchIfEmpty("Replaced".toMono())
      .subscribe {
        println(it)
      }

    println(2)
  }
}