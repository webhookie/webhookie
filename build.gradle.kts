import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
	id("org.springframework.boot") version "2.4.2"
	id("io.spring.dependency-management") version "1.0.10.RELEASE"
	kotlin("jvm") version "1.4.10"
	kotlin("plugin.spring") version "1.4.10"
	kotlin("kapt") version "1.4.20"
}

group = "com.hookiesolutions.webhookie"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repository-master.mulesoft.org/nexus/content/repositories/releases") }
	maven { url = uri("https://jitpack.io") }
	mavenCentral()
}

extra["springdocVersion"] = "1.5.2"
extra["jsonPathVersion"] = "2.5.0"
extra["bolEncryptedVersion"] = "2.6.0"
extra["amfVersion"] = "4.5.1"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.boot:spring-boot-starter-integration")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-aop")

	implementation("org.springframework.security:spring-security-oauth2-resource-server")
	implementation("org.springframework.security:spring-security-oauth2-jose")

	implementation("org.springdoc:springdoc-openapi-webflux-ui:${property("springdocVersion")}")
	implementation("org.springdoc:springdoc-openapi-kotlin:${property("springdocVersion")}")
	implementation("org.springdoc:springdoc-openapi-security:${property("springdocVersion")}")

	implementation("org.springframework.integration:spring-integration-webflux")
	implementation("org.springframework.integration:spring-integration-amqp")
	implementation("org.springframework.integration:spring-integration-mongodb")

	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	compileOnly("com.github.amlorg:amf-client_2.12:${property("amfVersion")}")

	implementation("com.jayway.jsonpath:json-path:${property("jsonPathVersion")}")

	implementation("com.bol:spring-data-mongodb-encrypt:${property("bolEncryptedVersion")}")

	compileOnly("org.projectlombok:lombok")

	compileOnly("io.projectreactor:reactor-tools")

	developmentOnly("org.springframework.boot:spring-boot-devtools")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.amqp:spring-rabbit-test")
	testImplementation("org.springframework.integration:spring-integration-test")
	testImplementation("org.springframework.security:spring-security-test")

	kapt("org.springframework.boot:spring-boot-configuration-processor")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.getByName<BootBuildImage>("bootBuildImage") {
	version = project.version.toString().replace("-SNAPSHOT", "")
	imageName = "hookiesolutions/${project.name}:$version"
}

configure<SourceSetContainer> {
	named("main") {
		java.srcDir("src/main/kotlin")
	}
}
