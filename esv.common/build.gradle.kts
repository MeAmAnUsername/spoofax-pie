plugins {
  id("org.metaborg.gradle.config.java-library")
}

val spoofaxVersion = extra["spoofaxVersion"] as String

dependencies {
  api(project(":common"))
  api("org.metaborg:org.spoofax.terms:$spoofaxVersion")
  implementation("org.metaborg:org.spoofax.jsglr:$spoofaxVersion") // TODO: avoid dependency to jsglr, only need it for imploder attachment.
  compileOnly("org.checkerframework:checker-qual-android:2.6.0") // Use android version: annotation retention policy is class instead of runtime.
}
