plugins {
  id("org.metaborg.gradle.config.java-library")
  id("org.metaborg.gradle.config.junit-testing")
}

fun compositeBuild(name: String) = "$group:$name:$version"

dependencies {
  api(platform(compositeBuild("spoofax.depconstraints")))
  annotationProcessor(platform(compositeBuild("spoofax.depconstraints")))


  api(compositeBuild("common"))
  api(compositeBuild("spoofax.core"))
  api(compositeBuild("spoofax.compiler"))
  api(project(":spoofax.lwb.compiler.cfg"))
  api("org.metaborg:resource")
  api("org.metaborg:pie.api")
  api("org.metaborg:pie.task.archive")
  api("org.metaborg:pie.task.java")

  // TODO: should the meta-languages use implementation configuration? We don't expose their API AFAICS.
  api(project(":cfg"))
  api(project(":sdf3"))
  api(project(":stratego"))
  api(project(":esv"))
  api(project(":statix"))

  api(project(":libspoofax2"))
  api(project(":libstatix"))

  // Using api configuration to make these annotations and processors available to javac that we call during
  // compilation, and to users of this library as well.
  api("org.checkerframework:checker-qual-android")
  api("com.google.dagger:dagger-compiler")

  // Convenient library to get the current classpath, which works under OSGi (Eclipse) as well. Used to pass the current
  // classpath to the Java compiler.
  // TODO: only using this to extract a classpath, can we just copy that functionality without a dependency?
  implementation("io.github.classgraph:classgraph:4.8.102")


  compileOnly("org.immutables:value-annotations")
  compileOnly("org.derive4j:derive4j-annotation")

  annotationProcessor("org.immutables:value")
  annotationProcessor("org.derive4j:derive4j")


  testImplementation("org.junit.jupiter:junit-jupiter-params:${metaborg.junitVersion}")
  testImplementation("org.metaborg:pie.runtime")
  testImplementation(project(":spoofax.lwb.compiler.dagger"))
  testCompileOnly("org.checkerframework:checker-qual-android")
}

tasks.test {
  // Show standard out and err in tests.
  testLogging {
    events(org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT, org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR)
    showStandardStreams = true
  }
}
