plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":client:notion"))
    implementation(project(":client:openAi"))
    implementation(project(":client:qiita"))
    implementation(libs.bundles.kotlin.bundle)
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
    useJUnit()
}
kotlin {
    jvmToolchain(17)
}