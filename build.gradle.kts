plugins {
  war
  base
  id("io.franzbecker.gradle-lombok") version Globals.lombokPluginVersion
  id("com.github.ben-manes.versions") version Globals.versionsPluginVersion
  id("fish.payara.micro-gradle-plugin") version Globals.payaraMicroPluginVersion
  // ./gradlew dependencyUpdates -Drevision=release
}

allprojects {
  group = Globals.groupId
  version = Globals.version
}

lombok {
  version = Globals.lombokVersion
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = sourceCompatibility
}

repositories {
  mavenCentral()
}

fun isAfterJdk8(): Boolean {
  val currentJavaVersion = org.gradle.internal.jvm.Jvm.current().javaVersion ?: JavaVersion.VERSION_1_8
  return currentJavaVersion.ordinal > JavaVersion.VERSION_1_8.ordinal
}

dependencies {
  // Liquibase
  implementation("org.liquibase:liquibase-core:${Globals.liquibaseVersion}")
  implementation("org.liquibase:liquibase-cdi:${Globals.liquibaseVersion}")

  // JPA
  implementation("com.h2database:h2:${Globals.h2Version}")
  providedCompile("javax.persistence:javax.persistence-api:${Globals.javaxPersistenceVersion}")

  if (isAfterJdk8()) { // JDK > 1.8
    implementation("javax.annotation:javax.annotation-api:${Globals.javaxAnnotationApiVersion}")
    implementation("javax.xml.bind:jaxb-api:${Globals.jaxbApiVersion}")
    implementation("org.glassfish.jaxb:jaxb-runtime:${Globals.jaxbRuntimeVersion}")
    implementation("org.javassist:javassist:${Globals.javassistVersion}")
    implementation("cglib:cglib-nodep:${Globals.cglibVersion}")
  }

  implementation(platform("org.junit:junit-bom:${Globals.junitJupiterVersion}"))
  implementation(platform("org.apache.logging.log4j:log4j-bom:${Globals.log4jVersion}"))
  implementation(platform("org.eclipse.microprofile:microprofile:${Globals.microprofileVersion}"))

  providedCompile("javax:javaee-api:${Globals.javaeeVersion}")

  implementation("org.webjars:materializecss:${Globals.materializecssVersion}")
  implementation("org.webjars:material-design-icons:${Globals.materialDesignIconsVersion}")
  implementation("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:${Globals.jacksonVersion}")

  testImplementation("org.assertj:assertj-core:${Globals.assertjVersion}")
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

val defaultJavaOpts = mapOf(
  "Xdebug" to null,
  "Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005" to null
)
val java9plusOpts = mapOf(
  "-illegal-access=permit" to null,
  "-add-modules=java.se" to null,
  "-add-exports=java.base/jdk.internal.ref=ALL-UNNAMED" to null,
  "-add-opens=java.base/java.lang=ALL-UNNAMED" to null,
  "-add-opens=java.base/java.nio=ALL-UNNAMED" to null,
  "-add-opens=java.base/sun.nio.ch=ALL-UNNAMED" to null,
  "-add-opens=java.management/sun.management=ALL-UNNAMED" to null,
  "-add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED" to null,
  "-add-opens=java.base/jdk.internal.loader=ALL-UNNAMED" to null,
  "-add-opens=jdk.zipfs/jdk.nio.zipfs=ALL-UNNAMED" to null
)

payaraMicro {
  daemon = false
  deployWar = false
  useUberJar = true
  payaraVersion = Globals.payaraMicroVersion
  commandLineOptions = mapOf("port" to 8080)
  javaCommandLineOptions =
      if (!isAfterJdk8()) defaultJavaOpts
      else defaultJavaOpts.plus(java9plusOpts)
}

tasks {
  val warTask = war.get()
  val cleanTask = clean.get()
  val assembleTask = assemble.get()
  val bundleTask = microBundle.get()
  val startTask = microStart.get()

  startTask.dependsOn(assembleTask.path)
  assembleTask.dependsOn(warTask.path, bundleTask.path)

  startTask.shouldRunAfter(cleanTask.path, assembleTask.path)
  assembleTask.shouldRunAfter(cleanTask.path, bundleTask.path)
  bundleTask.shouldRunAfter(cleanTask.path, warTask.path)
  warTask.shouldRunAfter(cleanTask.path)

  println("${org.gradle.internal.jvm.Jvm.current()} / ${org.gradle.util.GradleVersion.current()}")

  this.war {
    archiveFileName.set("ROOT.war")
  }

  named("clean") {
    delete(
        "$projectDir/out",
        buildDir
    )
  }

  withType<Wrapper> {
    gradleVersion = Globals.gradleWrapperVersion
    distributionType = Wrapper.DistributionType.BIN
  }
}

defaultTasks("clean", "build")
