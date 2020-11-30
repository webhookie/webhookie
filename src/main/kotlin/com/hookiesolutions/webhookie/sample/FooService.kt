package com.hookiesolutions.webhookie.sample

import com.hookiesolutions.webhookie.sample.model.Foo
import org.slf4j.Logger
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.stereotype.Service

@Service
class FooService(
	private val log: Logger
) {
	@ServiceActivator(inputChannel = "serviceChannel")
	fun service(foo: Foo): String {
		log.info("{} works!", foo.name)
		return "OK"
	}
}