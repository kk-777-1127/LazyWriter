plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.client.cio)
    implementation(libs.bundles.kotlin.bundle)
    implementation(libs.openai.client)
    implementation(project(":common"))
    implementation(project(":domain"))
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