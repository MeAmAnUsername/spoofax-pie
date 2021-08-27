plugins {
  id("org.metaborg.gradle.config.java-library")
}

val pieVersion = "15.0.0"

dependencies {
  implementation("org.metaborg", "pie.api", pieVersion)
  implementation("org.metaborg", "pie.runtime", pieVersion)
}
