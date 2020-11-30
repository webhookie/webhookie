package com.hookiesolutions.webhookie.sample

import com.hookiesolutions.webhookie.sample.model.Foo
import com.hookiesolutions.webhookie.sample.model.FooPublisher
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.context.IntegrationFlowContext
import org.springframework.integration.webflux.dsl.WebFlux
import org.springframework.stereotype.Component

@Component
class FooFlowFactory(
	private val ctx: IntegrationFlowContext
) {
	fun generate(fooPublisher: FooPublisher): IntegrationFlow {
		val inboundGateway = WebFlux.inboundGateway(fooPublisher.path)
			.autoStartup(true)
			.requestMapping {
				it
					.methods(HttpMethod.POST)
					.produces(MediaType.TEXT_PLAIN_VALUE)
					.consumes(fooPublisher.mediaType.toString())
			}
			.requestPayloadType(Foo::class.java)

		return IntegrationFlows.from(inboundGateway)
			.channel("serviceChannel")
			.get()!!
	}

	fun register(fooPublisher: FooPublisher) {
		ctx.registration(generate(fooPublisher))
			.register()
	}
}