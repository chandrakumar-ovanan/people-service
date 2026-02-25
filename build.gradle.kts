import com.google.cloud.tools.jib.api.buildplan.ImageFormat

plugins {
    java
    id("idea")
    `jvm-test-suite`
    jacoco
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.google.jib)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.owasp.dependencycheck)
    alias(libs.plugins.freefair.lombok)
    alias(libs.plugins.spotless)
}

val testcontainersVersion = libs.versions.testcontainers.get()
val jvmPreviewArg = "--enable-preview"

repositories {
    mavenCentral()
}

testing {
    suites {
        val integrationTest = register<JvmTestSuite>("integrationTest") {
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

dependencies {
    implementation(platform(libs.spring.ai.bom))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation(libs.springdoc.openapi.webmvc.ui)
    implementation("org.springframework.ai:spring-ai-starter-model-ollama")
    implementation("org.springframework.ai:spring-ai-starter-vector-store-pgvector")
    implementation("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.bundles.testcontainers)
}

configurations.testRuntimeClasspath {
    resolutionStrategy.force(
        "org.testcontainers:testcontainers:$testcontainersVersion",
        "org.testcontainers:jdbc:$testcontainersVersion",
        "org.testcontainers:database-commons:$testcontainersVersion"
    )
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

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs(jvmPreviewArg)
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add(jvmPreviewArg)
}

spotless.java {
    googleJavaFormat()
    targetExclude("build/**")
}

tasks.jacocoTestReport {
    dependsOn(tasks.named("test"), tasks.named("integrationTest"))
    reports {
        xml.required = true
        csv.required = true
    }
}

sonarqube {
    val testDirs = listOf("test", "integrationTest")
        .mapNotNull { name ->
            sourceSets.findByName(name)
                ?.allSource
                ?.srcDirs
                ?.filter { it.exists() && it.name != "resources" }
                ?.map { it.path }
        }
        .flatten()
    properties {
        if (testDirs.isNotEmpty()) {
            properties["sonar.tests"] = testDirs.joinToString(",")
        }
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.token", findProperty("sonar.token") ?: "")
        property("sonar.exclusions", "**/entity/**,**/model/**,**/build/generated/**")
    }
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(tasks.named("openApiGenerate"))
}

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

jib {
    val registry = "harbor.plavonra.com"
    val baseImage = "$registry/platform-base/java:25-distroless"
    val imageName = "$registry/apps/${project.name}:${project.version}"
    val authUser = findProperty("harbor.username")?.toString() ?: System.getenv("HARBOR_USERNAME") ?: ""
    val authPassword = findProperty("harbor.password")?.toString() ?: System.getenv("HARBOR_PASSWORD") ?: ""

    from {
        image = baseImage
        if (authUser.isNotEmpty()) {
            auth {
                username = authUser
                password = authPassword
            }
        }
    }
    to {
        image = imageName
        if (authUser.isNotEmpty()) {
            auth {
                username = authUser
                password = authPassword
            }
        }
    }
    container {
        ports = listOf("8080")
        jvmFlags = listOf("-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75")
        creationTime.set("USE_CURRENT_TIMESTAMP")
        format = ImageFormat.OCI
    }
}
