import org.apache.tools.ant.taskdefs.condition.Os

plugins {
  war
  base
  id("io.franzbecker.gradle-lombok") version "3.0.0"
  id("com.github.ben-manes.versions") version "0.21.0"
  id("io.spring.dependency-management") version "1.0.7.RELEASE"
}

lombok {
  val lombokVersion: String by project
  version = lombokVersion
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

dependencies {
  // 5.183 is broken, Uber Jar is fixed with 5.184
  val payaraMicroVersion: String by project
  payaraMicro("fish.payara.extras:payara-micro:$payaraMicroVersion")

  val log4jVersion: String by project
  val javaeeVersion: String by project
  val jacksonVersion: String by project
  val assertjVersion: String by project
  val junitJupiterVersion: String by project
  val microprofileVersion: String by project
  val materializecssVersion: String by project
  val materialDesignIconsVersion: String by project

  implementation(platform("org.junit:junit-bom:$junitJupiterVersion"))
  implementation(platform("org.apache.logging.log4j:log4j-bom:$log4jVersion"))
  implementation(platform("org.eclipse.microprofile:microprofile:$microprofileVersion"))

  providedCompile("javax:javaee-api:$javaeeVersion")

  implementation("org.webjars:materializecss:$materializecssVersion")
  implementation("org.webjars:material-design-icons:$materialDesignIconsVersion")
  implementation("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:$jacksonVersion")

  testImplementation("org.assertj:assertj-core:$assertjVersion")
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

  register("bundle", Exec::class.java) {
    group = "PayaraMicro"
    description = "build payara uber jar from war"
    commandLine(getCommand("$getDeployCommand --outputUberJar $getOutputUberJar"))
    shouldRunAfter("clean", "war")
    dependsOn("war")
  }

  register("start", Exec::class.java) {
    group = "PayaraMicro"
    description = "java -jar $getOutputUberJar"
    commandLine(getCommand("java -jar $getOutputUberJar"))
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
    val gradleWrapperVersion: String by project
    gradleVersion = gradleWrapperVersion
    distributionType = Wrapper.DistributionType.BIN
  }
}

defaultTasks("clean", "bundle")
