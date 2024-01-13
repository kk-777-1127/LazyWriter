import com.github.gradle.node.NodeExtension
import com.github.gradle.node.task.NodeTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    id("com.github.node-gradle.node") version "7.0.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":common"))
    implementation(project(":client:processor"))
    implementation(project(":client:qiita"))
    implementation(project(":client:notion"))
    implementation(project(":client:openAi"))
    implementation(libs.openai.client)
    implementation(libs.bundles.ktor.clients)
    implementation(libs.bundles.kotlin.bundle)

    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")

}

kotlin {
    jvmToolchain(17)
}

// TODO
// Node Process Tasks
configure<NodeExtension> {
    workDir = file("NotionToMd")
    nodeProjectDir = file("NotionToMd")
}
// Node.jsサーバーを実行するタスク
tasks.register<NodeTask>("runServer") {
    script = file("NotionToMd/index.js")
}

// Node.jsサーバーの実行とnpm依存関係のインストールを組み合わせたタスク
tasks.register("listenPageIdServer") {
    dependsOn("npmInstall", "runServer")
}

tasks.register("killNodeProcess") {
    doLast {
        val pidFile = File("server.pid")
        if (pidFile.exists()) {
            val pid = pidFile.readText().trim()
            try {
                Runtime.getRuntime().exec("kill $pid")
                println("Node.jsプロセス（PID: $pid）が終了しました。")
            } catch (e: Exception) {
                println("Node.jsプロセスの終了中にエラーが発生しました: ${e.message}")
            } finally {
                pidFile.delete()
            }
        } else {
            println("PIDファイルが見つかりませんでした。")
        }
    }
}
