plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

val pieVersion = "15.0.0"

dependencies {
  implementation("org.metaborg", "pie.api", pieVersion)
  implementation("org.metaborg", "pie.runtime", pieVersion)
  implementation("org.metaborg", "common", "5.0.0")

  compileOnly("org.checkerframework:checker-qual-android")
}

task<JavaExec>("evaluate") {
  group = "Evaluation"
  description = "Evaluate the case studies for Ivo's thesis"
  classpath = sourceSets["main"].runtimeClasspath
  main = "pl.thesis.evaluation.Main"
}

task("buildAndEvaluate") {
  dependsOn("build", "evaluate")
}

tasks.withType<Test> {
  this.testLogging {
    outputs.upToDateWhen {false}
    this.showStandardStreams = true
  }
}
