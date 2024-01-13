plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.kotlin.bundle)
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(libs.bundles.kotlin.test.bundle)
}

tasks.test {
    useJUnitPlatform()
    useJUnit()
}
kotlin {
    jvmToolchain(17)
}