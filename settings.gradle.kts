dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.version.toml"))
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "LazyWriter"
include("client:openAi")
include("client:notion")
include("client:qiita")
include("domain")
include("common")
include("client:processor")
findProject(":client:processor")?.name = "processor"
