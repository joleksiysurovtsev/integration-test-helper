import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import pl.allegro.tech.build.axion.release.domain.hooks.HookContext
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("pl.allegro.tech.build.axion-release") version "1.18.16"
    id("com.gorylenko.gradle-git-properties") version "2.4.2"
    `java-library`
    `maven-publish`
}

group = "dev.surovtsev.integration"
version = scmVersion.version +"-2"

repositories {
    mavenLocal()
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

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(sourcesJar.get())
            groupId = project.group.toString()
            version = project.version.toString()
            repositories {
                mavenLocal()
            }
        }
    }
}

sourceSets{
    main{
        kotlin.srcDirs("src/main/kotlin")
        java.srcDirs("src/main/java")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}



gitProperties {
    failOnNoGitDirectory = false
    keys = mutableListOf(
        "git.commit.id",
        "git.commit.time",
        "git.branch",
        "git.build.version",
        "git.commit.message.full",
        "git.commit.user.name",
        "git.commit.id.abbrev"
    )
}

scmVersion {
    useHighestVersion.set(true)
    branchVersionIncrementer.set(
        mapOf(
            "develop.*" to "incrementPatch",
            "hotfix.*" to listOf("incrementPrerelease", mapOf("initialPreReleaseIfNotOnPrerelease" to "fx.1")),
        )
    )

    branchVersionCreator.set(
        mapOf(
            "master" to KotlinClosure2({ v: String, s: ScmPosition -> "${v}" }),
            ".*" to KotlinClosure2({ v: String, s: ScmPosition -> "$v-${s.branch}" }),
        )
    )

    snapshotCreator.set { versionFromTag: String, scmPosition: ScmPosition -> "-${scmPosition.shortRevision}" }
    tag.initialVersion.set { tagProperties: TagProperties, _: ScmPosition -> "4.0.0" }
    tag.prefix.set("v")
    tag.versionSeparator.set("")

    hooks {
        post { hookContext: HookContext ->
            println("scmVersion previousVersion: ${hookContext.previousVersion}")
            println("scmVersion  releaseVersion: ${hookContext.releaseVersion}")
        }
    }
}

tasks.named("publish").configure {
    doLast {
        println ("Опубликован артефакт: ${project.group}:${rootProject.name}:${project.version}")
    }
}