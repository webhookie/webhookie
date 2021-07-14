# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.4.0/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.4.0/gradle-plugin/reference/html/#build-image)
* [Coroutines section of the Spring Framework Documentation](https://docs.spring.io/spring/docs/5.3.1/spring-framework-reference/languages.html#coroutines)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/2.4.0/reference/htmlsingle/#using-boot-devtools)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/docs/2.4.0/reference/htmlsingle/#configuration-metadata-annotation-processor)
* [Spring Integration](https://docs.spring.io/spring-boot/docs/2.4.0/reference/htmlsingle/#boot-features-integration)
* [Spring Security](https://docs.spring.io/spring-boot/docs/2.4.0/reference/htmlsingle/#boot-features-security)
* [Spring Data Reactive MongoDB](https://docs.spring.io/spring-boot/docs/2.4.0/reference/htmlsingle/#boot-features-mongodb)
* [Spring for RabbitMQ](https://docs.spring.io/spring-boot/docs/2.4.0/reference/htmlsingle/#boot-features-amqp)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/2.4.0/reference/htmlsingle/#production-ready)
* [Resilience4J](https://cloud.spring.io/spring-cloud-static/spring-cloud-circuitbreaker/current/reference/html)

### Guides
The following guides illustrate how to use some features concretely:

* [Integrating Data](https://spring.io/guides/gs/integration/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Messaging with RabbitMQ](https://spring.io/guides/gs/messaging-rabbitmq/)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
* [Securing REST API using Keycloak and Spring Oauth2](https://medium.com/@bcarunmail/securing-rest-api-using-keycloak-and-spring-oauth2-6ddf3a1efcc2)
* [A Quick Guide to Using Keycloak with Spring Boot](https://www.baeldung.com/spring-boot-keycloak)
* [Keycloack openid-configuration](http://127.0.0.1:8000/auth/realms/webhookie/.well-known/openid-configuration)
* [guaranteed-delivery-in-spring-integration](https://xebia.com/blog/guaranteed-delivery-in-spring-integration/)
* [Spring Integration Java DSL](https://dzone.com/articles/spring-integration-java-dsl-1)
* [Concurrency in Spring WebFlux](https://www.baeldung.com/spring-webflux-concurrency)

### Mongodb
* [mongodb-aggregation-framework-working-with-arrays](https://www.codementor.io/@prasadsaya/mongodb-aggregation-framework-working-with-arrays-18jd5fe2xo)

### Webhookie and AWS related links
* [A Scalable, Reliable Webhook Dispatcher Powered by Kafka](https://medium.com/hootsuite-engineering/a-scalable-reliable-webhook-dispatcher-powered-by-kafka-2dc3d677f16b)
* [Creating container products for AWS Marketplace using Amazon EKS and AWS Fargate](https://aws.amazon.com/blogs/awsmarketplace/creating-container-products-for-aws-marketplace-using-amazon-eks-and-aws-fargate/)
* [AWS Marketplace - Seller Guide](https://docs.aws.amazon.com/marketplace/latest/userguide/container-product-getting-started.html)


Steps:
 - 4 - Creating a container product in the AWS Marketplace Management Portal
 - 5 - Building and uploading the product’s image
 -

Kotlin:
 - [Generics in Kotlin](https://medium.com/swlh/generics-in-kotlin-5152142e281c)

Auth:
 - [angular-oauth2-oidc](https://manfredsteyer.github.io/angular-oauth2-oidc/docs/additional-documentation/authorization-servers/auth0.html)

```
  @Bean
  fun eventPublisherChannelFlow(
    connectionFactory: ConnectionFactory,
    amqpTemplate: AmqpTemplate
  ): IntegrationFlow {
    val outboundGateway = Amqp.outboundAdapter(amqpTemplate)
      .routingKey("wh-event")
      .exchangeName("wh-customer")
    return IntegrationFlows
      .from(eventPublisherChannel)
      .log<Message<*>> { log.info("{}", it) }
      .handle(outboundGateway)
      .nullChannel()
  }

  @Bean
  fun container(
    connectionFactory: ConnectionFactory,
  ): SimpleMessageListenerContainer {
    val container = SimpleMessageListenerContainer()
    container.connectionFactory = connectionFactory
    container.setQueueNames("wh-customer.event")
    return container
  }

  @Bean("wh-customer.event.dlq")
  fun dlq(): Queue {
    return QueueBuilder.durable("wh-customer.event.dlq")
      .build()
  }

  @Bean("DLX.exchange")
  fun dlqExchange(): DirectExchange {
    return DirectExchange("DLX", true, false)
  }

  @Bean("wh-customer.event.dlq.binding")
  fun dlqBinding(dlqExchange: DirectExchange): Binding {
    return Binding(dlq().name, Binding.DestinationType.QUEUE, dlqExchange.name, "wh-event", emptyMap())
  }

  @ServiceActivator(inputChannel = "customerEventInChannel", outputChannel = "subscriptionInChannel")
  fun eventFlowActivator(
    message: Message<*>,
    @Header(WH_HEADER_TOPIC, required = true) topic: String,
    @Header(WH_HEADER_TRACE_ID, required = true) traceId: String,
    @Header(HEADER_CONTENT_TYPE, required = true) contentType: String,
    @Header(WH_HEADER_AUTHORIZED_SUBSCRIBER, required = false, defaultValue = "") authorizedSubscribers: List<String> = emptyList()
  ): Message<*> {
    log.info("{}", message.payload)
    log.info("{}", message.headers)
    log.info("{}", topic)

    return message
  }

  @Bean
  fun customerEventConsumer(): Consumer<Message<Any>> {
    return Consumer {
      log.info("{}", it)
    }
  }
```
