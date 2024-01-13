plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.ktor.clients)
    implementation(libs.bundles.kotlin.bundle)
    implementation(project(":domain"))
    implementation(project(":common"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(libs.bundles.kotlin.test.bundle)
    testImplementation(libs.bundles.ktor.clients.test)
}

tasks.test {
    useJUnitPlatform()
    useJUnit()
}

kotlin {
    jvmToolchain(17)
}