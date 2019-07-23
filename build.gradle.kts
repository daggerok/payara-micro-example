import org.apache.tools.ant.taskdefs.condition.Os

plugins {
  war
  base
  id("io.franzbecker.gradle-lombok") version Globals.lombokPluginVersion
  id("com.github.ben-manes.versions") version Globals.versionsPluginVersion
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
  targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
  mavenCentral()
}

val payaraMicro by configurations.creating
configurations {
  payaraMicro
}

fun isAfterJdk8(): Boolean {
  val currentJavaVersion = org.gradle.internal.jvm.Jvm.current().javaVersion ?: JavaVersion.VERSION_1_8
  return currentJavaVersion.ordinal > JavaVersion.VERSION_1_8.ordinal
}

dependencies {
  if (isAfterJdk8()) {
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:2.3.2")
    implementation("org.javassist:javassist:3.23.1-GA")
    implementation("cglib:cglib-nodep:3.2.7")
  }
  // 5.183 is broken, Uber Jar is fixed with 5.184
  payaraMicro("fish.payara.extras:payara-micro:${Globals.payaraMicroVersion}")

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

tasks {
  assemble.get().dependsOn("bundle")
  // https://docs.gradle.org/current/userguide/war_plugin.html
  // workaround for context-root: "/"
  this.war {
    archiveFileName.set("ROOT.war")
  }

  val payaraMicroJar = configurations["payaraMicro"].asPath
  val getDeployCommand = "java -jar $payaraMicroJar --autoBindHttp --clusterName app --deploy ${this.container.war.get().archiveFile.get()}"
  val getOutputUberJar = "$buildDir/$name-$version-microbundle.jar"

  fun getCommand(vararg suffix: String): Iterable<String> {
    val prefix = if (Os.isFamily(Os.FAMILY_WINDOWS)) 
      arrayOf("cmd", "/c") else arrayOf("sh", "-c")
    return prefix.plus(suffix).toList()
  }

  println("${org.gradle.internal.jvm.Jvm.current()} / ${org.gradle.util.GradleVersion.current()}")

  register("bundle", Exec::class.java) {
    group = "PayaraMicro"
    description = "build payara uber jar from war"
    commandLine(getCommand("$getDeployCommand --outputUberJar $getOutputUberJar"))
    shouldRunAfter("clean", "war")
    dependsOn("war")
  }

  register("start", Exec::class.java) {
    group = "PayaraMicro"
    val jdk9Opts = if (!isAfterJdk8()) ""
    else "--add-modules java.se" +
        " --add-exports java.base/jdk.internal.ref=ALL-UNNAMED" +
        " --add-opens java.base/java.lang=ALL-UNNAMED" +
        " --add-opens java.base/java.nio=ALL-UNNAMED" +
        " --add-opens java.base/sun.nio.ch=ALL-UNNAMED" +
        " --add-opens java.management/sun.management=ALL-UNNAMED" +
        " --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED" +
        " --add-opens java.base/jdk.internal.loader=ALL-UNNAMED" +
        " --add-opens jdk.zipfs/jdk.nio.zipfs=ALL-UNNAMED"
    description = "java -jar $getOutputUberJar"
    commandLine(getCommand("java $jdk9Opts -jar $getOutputUberJar"))
    shouldRunAfter("clean", "war", "bundle")
    dependsOn("bundle")
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

defaultTasks("clean", "bundle")
