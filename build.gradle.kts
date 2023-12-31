java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

plugins {
    application

    // id("com.diffplug.spotless") version "6.23.3"
    id("com.bmuschko.docker-java-application") version "9.4.0"
    id("com.dorongold.task-tree") version "2.1.1"
    id("com.github.mrsarm.jshell.plugin") version "1.2.1"
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testImplementation("org.mockito:mockito-core:5.1.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.1.1")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("com.google.guava:guava:32.1.1-jre")

    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("org.slf4j:slf4j-simple:2.0.9")

    implementation("com.rethinkdb:rethinkdb-driver:2.4.4")
    implementation("commons-logging:commons-logging:1.3.0")

    implementation("rs.ltt.jmap:jmap-common:0.8.18")
    implementation("rs.ltt.jmap:jmap-gson:0.8.18")
    implementation("com.google.code.gson:gson:2.10.1")
    // implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")

    implementation("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    implementation("com.squareup.okio:okio:3.7.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    //testLogging.showStandardStreams = true
}

application {
    mainClass.set("it.unipd.overture.jmap.Ignite")
}

docker {
    javaApplication {
        baseImage.set("openjdk:21-jdk-slim")
        maintainer.set("Overture 'overture.unipd@gmail.com'")
        ports.set(listOf(8000))
        images.set(setOf("overture-unipd/jmap:latest"))
        mainClassName.set("it.unipd.overture.jmap.Ignite")
        // jvmArgs.set(listOf("-Xms256m", "-Xmx2048m"))
    }
}

/*
configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    enforceCheck = false
}

spotless {
    java {
        googleJavaFormat()
    }
}
 */
