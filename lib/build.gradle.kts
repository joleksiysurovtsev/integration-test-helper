import org.gradle.internal.impldep.org.bouncycastle.asn1.crmf.SinglePubInfo.web

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    // ==== Core Spring dependencies ====
    implementation(libs.spring.context) // Spring IoC container (dependency injection, bean management)
    implementation(libs.spring.web)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.test) // Spring Boot testing utilities (includes JUnit, Mockito, etc.)
    implementation(libs.spring.jdbc) // Spring JDBC support for database interactions
    implementation(libs.spring.kafka) // Spring Kafka integration

    // ==== Spring boot dependencies ====
    implementation(libs.spring.test)
    implementation(libs.spring.data.commons) // Spring Data common components (e.g., repository support)

    // ==== TestContainers (integration testing with containers) ====
    implementation(libs.testcontainers) // Core Testcontainers library
    implementation(libs.testcontainers.kafka) // Testcontainers support for Kafka
    implementation(libs.testcontainers.postgresql) // Testcontainers support for PostgreSQL

    // ==== JUnit (testing framework) ====
    implementation(libs.junit.jupiter) // JUnit 5 testing framework API

    // ==== Apache Kafka dependencies ====
    implementation(libs.kafka.clients) // Kafka client for producer/consumer communication with Kafka broker

    // ==== JSON processing (Jackson) ====
    implementation(libs.jackson.databind) // Core Jackson library for JSON serialization/deserialization
    implementation(libs.jackson.datatype.jsr310) // Jackson module for Java 8+ date/time support (`java.time`)
    implementation(libs.jackson.module.kotlin)// Jackson module for better Kotlin compatibility
    implementation(libs.json.simple) // Lightweight JSON parsing library

    // ==== Miscellaneous ====
    implementation(libs.kotlin.reflect) // Kotlin reflection support (required for some serialization/deserialization)

}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.11.1")
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
