plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.1.10"
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.4.4"
    id("io.micronaut.aot") version "4.4.5"
}

version = "0.1"
group = "dev.dinkleberg"

val kotlinVersion = project.properties.get("kotlinVersion")
repositories {
    mavenCentral()
    maven { url = uri("https://m2.dv8tion.net/releases") }
}

dependencies {
    ksp("io.micronaut.data:micronaut-data-processor")
    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.serde:micronaut-serde-processor")
    ksp("io.micronaut.openapi:micronaut-openapi")
    implementation("io.micronaut.data:micronaut-data-r2dbc")
    implementation("io.micronaut.flyway:micronaut-flyway")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.4")
    compileOnly("io.micronaut:micronaut-http-client")
    compileOnly("io.micronaut.openapi:micronaut-openapi-annotations")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("io.r2dbc:r2dbc-h2")
    runtimeOnly("io.r2dbc:r2dbc-pool:1.0.2.RELEASE")
    runtimeOnly("org.yaml:snakeyaml")
    testImplementation("io.micronaut:micronaut-http-client")
    implementation("com.sedmelluq:lavaplayer:1.3.78")
    implementation("io.micronaut.views:micronaut-views-freemarker")
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.1")
    implementation("io.micronaut.cache:micronaut-cache-caffeine")
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
    implementation("net.bramp.ffmpeg:ffmpeg:0.8.0")
    implementation("io.swagger.core.v3:swagger-annotations")

    implementation("dev.kord:kord-common:0.15.0")
    implementation("dev.kord:kord-rest:0.15.0")
    implementation("dev.kord:kord-gateway:0.15.0")
    implementation("dev.kord:kord-core:0.15.0")
    implementation("dev.kord:kord-voice:0.15.0")
    implementation("dev.kord:kord-core-voice:0.15.0")
}


application {
    mainClass.set("dev.dinkleberg.soundboard.bot.Application")
}
java {
    sourceCompatibility = JavaVersion.toVersion("21")
}
kotlin {
    jvmToolchain(21)
}

graalvmNative.toolchainDetection.set(false)
micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("dev.dinkleberg.soundboard.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading.set(false)
        convertYamlToJava.set(false)
        precomputeOperations.set(true)
        cacheEnvironment.set(true)
        optimizeClassLoading.set(true)
        deduceEnvironment.set(true)
        optimizeNetty.set(true)
    }
}

tasks.named<io.micronaut.gradle.docker.DockerBuildOptions>("dockerfile") {
    editDockerfile {
        after("FROM eclipse-temurin:21-jre-jammy") {
            insert(
                "RUN apt update && apt install software-properties-common -y && add-apt-repository ppa:tomtomtom/yt-dlp && apt update && apt install ffmpeg yt-dlp -y",
                "ENV MICRONAUT_ENVIRONMENTS=prod"
            )
        }

    }
}

tasks.named<io.micronaut.gradle.docker.MicronautDockerfile>("dockerfile") {
    baseImage("eclipse-temurin:21-jre-jammy")
    args("-Xmx1g", "-Xdebug", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-XX:MaxHeapFreeRatio=10", "-XX:MinHeapFreeRatio=10", "-XX:MaxDirectMemorySize=256m", "-XX:+HeapDumpOnOutOfMemoryError")
}

tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion.set("21")
}

tasks.named<JavaExec>("run") {
    doFirst {
        environment("MICRONAUT_ENVIRONMENTS", "local")
        if (project.file(".env").exists()) {
            file(".env").readLines().forEach {
                if (it.isNotEmpty() && !it.startsWith("#")) {
                    val (key, value) = it.split('=', limit = 2)
                    if (System.getenv(key) == null) {
                        environment(key, value)
                    }
                }
            }
        } else {
            println("No custom environment variables set")
        }
    }
}
