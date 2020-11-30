package com.hookiesolutions.webhookie.sample

import com.hookiesolutions.webhookie.sample.model.FooPublisher
import com.hookiesolutions.webhookie.sample.model.FooPublisherRepository
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import javax.validation.Valid

@RestController
class HomeController(
	private val fooFlowFactory: FooFlowFactory,
	private val log: Logger,
	private val repository: FooPublisherRepository
) {
	@PostMapping("/create", produces = [MediaType.TEXT_PLAIN_VALUE])
	fun createFlow(@RequestBody @Valid body: FooPublisherRequest): Mono<String> {
		return repository.save(body.publisher())
			.doOnNext { log.info("Saving publisher: '{}'", it) }
			.doOnNext { fooFlowFactory.register(it) }
			.map { it.id!! }
	}

	@GetMapping("/")
	fun home(): Mono<String> {
		return "OK".toMono()
	}
}

data class FooPublisherRequest(
	val name: String,
	val path: String,
	val mediaType: String = MediaType.APPLICATION_JSON_VALUE
) {
	fun publisher(): FooPublisher {
		return FooPublisher(name, path, true, MediaType.parseMediaType(mediaType))
	}
}