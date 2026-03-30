import com.google.cloud.tools.jib.api.buildplan.ImageFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    jacoco
    `jvm-test-suite`
    alias(libs.plugins.google.jib)
    alias(libs.plugins.freefair.lombok)
    alias(libs.plugins.spotless)
    alias(libs.plugins.openapi.generator)
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()
description = providers.gradleProperty("description").get()

testing {
    suites {
        register<JvmTestSuite>("integrationTest") {
            useJUnitJupiter()
            dependencies {
                implementation(project())
            }
        }
    }
}

configurations.named("integrationTestImplementation") {
    extendsFrom(configurations.named("testImplementation").get())
}

apply(from = "${project.projectDir}/openapi-packages.gradle.kts")

val apiServerPackage: String by extra
val apiModelPackage: String by extra

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven {
      url = uri("https://nexus.plavonra.com/repository/maven-online-public/")
    }
}

dependencies {
    implementation(platform(libs.plavonra.spring.bom))
    annotationProcessor(platform(libs.plavonra.spring.bom))

    implementation("com.plavonra:plavonra-spring-ai-starter-langfuse")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    annotationProcessor("org.mapstruct:mapstruct-processor")
    implementation("org.mapstruct:mapstruct")

    implementation("org.springframework.ai:spring-ai-starter-model-ollama")
    implementation("org.springframework.ai:spring-ai-starter-vector-store-pgvector")

    runtimeOnly("io.micrometer:micrometer-tracing-bridge-otel")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation(libs.bundles.testcontainers)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events(
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.FAILED,
            TestLogEvent.STANDARD_OUT,
            TestLogEvent.STANDARD_ERROR,
        )
        showStandardStreams = true
    }
}

tasks.named<Test>("integrationTest") {
    shouldRunAfter(tasks.test)
}

tasks.check {
    dependsOn(tasks.named<Test>("integrationTest"))
}

tasks.matching { it.name == "spotlessJava" || it.name == "spotlessJavaApply" || it.name == "spotlessJavaCheck" }
    .configureEach {
        dependsOn(tasks.named("openApiGenerate"))
    }

spotless {
    java {
        googleJavaFormat()
    }
    format("gradle") {
        target("*.gradle.kts", "settings.gradle.kts", "gradle/*.gradle.kts")
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("misc") {
        target(".gitattributes", ".gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

val openApiConfigOptions = mapOf(
    "useTags" to "true",
    "interfaceOnly" to "true",
    "skipDefaultInterface" to "true",
    "useBeanValidation" to "true",
    "openApiNullable" to "false",
    "serializableModel" to "true",
    "bigDecimalAsString" to "false",
    "dateLibrary" to "java8",
    "documentationProvider" to "none",
    "useResponseEntity" to "false",
    "useSpringdocOpenApi" to "true",
    "useJakartaEe" to "true"
)
openApiGenerate {
    generatorName.set("spring")
    inputSpec.set(layout.projectDirectory.file("src/main/resources/openapi/people.yaml").asFile.path)
    outputDir.set(layout.buildDirectory.dir("generated").map { it.asFile.path })
    apiPackage.set(apiServerPackage)
    modelPackage.set(apiModelPackage)
    globalProperties.set(mapOf("apis" to "", "models" to ""))
    configOptions.set(openApiConfigOptions)
}

sourceSets.main {
    java.srcDir(layout.buildDirectory.dir("generated/src/main/java"))
}
tasks.named<JavaCompile>("compileJava") {
    dependsOn(tasks.named("openApiGenerate"))
}

tasks.jacocoTestReport {
    dependsOn(tasks.named("test"), tasks.named("integrationTest"))
    reports {
        xml.required = true
        csv.required = true
    }
}

jib {
    val registry = "registry.plavonra.com"
    from {
        image = "$registry/platform-base/java:21-distroless"
    }
    to {
        image =  "$registry/apps/${project.name}:${project.version}"
    }
    container {
        ports = listOf("8080")
        jvmFlags = listOf("-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75")
        creationTime.set("USE_CURRENT_TIMESTAMP")
        format = ImageFormat.OCI
    }
}
