rootProject.name = "SermilionBackend"

pluginManagement {
    includeBuild("build-logic")
}

include("app")
include("core:data")
include("core:domain")
include("core:presentation")