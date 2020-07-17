plugins {
    application
    kotlin("jvm") version "1.3.72"
}

group = "ru.serobyan"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.google.code.gson", "gson", "2.8.6")
    implementation("org.kodein.di", "kodein-di", "7.0.0")
    implementation("net.lightbody.bmp", "browsermob-core", "2.1.5")
    implementation("org.jsoup", "jsoup", "1.13.1")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.7")
    implementation("ch.qos.logback", "logback-core", "1.2.3")
    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("org.slf4j", "slf4j-api", "1.7.30")
    implementation("com.zaxxer", "HikariCP", "3.4.5")
    implementation("org.jetbrains.exposed", "exposed-core", "0.26.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.26.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.26.1")
    implementation("org.seleniumhq.selenium", "selenium-java", "3.141.59")
    implementation("org.xerial", "sqlite-jdbc", "3.31.1")
    implementation("com.zaxxer", "HikariCP", "3.4.2")
    implementation("io.ktor", "ktor-client-core", "1.3.2")
    implementation("io.ktor", "ktor-client-apache", "1.3.2")
    implementation("commons-io", "commons-io", "2.7")
    implementation("commons-cli", "commons-cli", "1.4")
    testImplementation("io.kotest", "kotest-runner-junit5-jvm", "4.0.6")
    testImplementation("io.kotest", "kotest-assertions-core-jvm", "4.0.6")
    testImplementation("io.kotest", "kotest-property-jvm", "4.0.6")
}

application {
    mainClassName = "ru.serobyan.vk_photo_crawler.MainKt"
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}