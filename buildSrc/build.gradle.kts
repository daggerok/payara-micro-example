
plugins {
  idea
  `kotlin-dsl`
}

kotlinDslPluginOptions {
  experimentalWarning.set(false)
}

repositories {
  gradlePluginPortal()
}

idea {
  module {
    isDownloadJavadoc = false
    isDownloadSources = false
  }
}
