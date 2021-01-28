package playground

import org.junit.jupiter.api.Test
import reactor.core.publisher.Sinks

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 28/1/21 13:47
 */
class SinksPlayground {
  @Test
  fun testSinksManyMulticast() {
    val hotSource = Sinks.many().multicast().directBestEffort<String>()

    val hotFlux = hotSource.asFlux().map { obj: String -> obj.toUpperCase() }.cache()


    hotFlux.subscribe { d: String -> println("Subscriber 1 to Hot Source: $d") }

    hotSource.tryEmitNext("blue")
    hotSource.tryEmitNext("green")

    hotFlux.subscribe { d: String -> println("Subscriber 2 to Hot Source: $d") }

    hotSource.tryEmitNext("orange")
    hotSource.tryEmitNext("purple")
    hotSource.tryEmitComplete()
  }

  @Test
  fun testSinks() {
    val hotSource = Sinks.many().multicast().directAllOrNothing<String>()
    val hotFlux = hotSource.asFlux()

    hotFlux.subscribe { println("Subscriber 1 -> $it") }

    hotSource.emitNext("blue", Sinks.EmitFailureHandler.FAIL_FAST)
    hotSource.emitNext("green", Sinks.EmitFailureHandler.FAIL_FAST)
    hotSource.emitNext("yellow") { t, r ->
      println(t)
      println(r)
      true
    }

    hotFlux.subscribe { println("Subscriber 2 -> $it") }

    hotSource.emitNext("orange", Sinks.EmitFailureHandler.FAIL_FAST)
    hotSource.emitNext("purple", Sinks.EmitFailureHandler.FAIL_FAST)
    hotSource.emitNext("red") { t, r ->
      println(t)
      println(r)
      true
    }

    val singleHotSource = Sinks.one<String>()
    val singleMono = singleHotSource.asMono()

    singleMono.subscribe { println("Mono 1 $it") }

    singleHotSource.tryEmitValue("Hello")
    singleMono.subscribe { println("Mono 2 $it") }
  }
}