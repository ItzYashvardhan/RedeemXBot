plugins {
    kotlin("jvm") version "2.1.21"
    id("com.gradleup.shadow") version "8.3.6"
}

group = "me.justlime"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven { url = uri("https://jitpack.io") }
    maven("https://repo.extendedclip.com/releases/") { name = "extendedclip" }

}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly(kotlin("stdlib"))
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.ItzYashvardhan:RedeemCodeX-API")
    implementation("net.dv8tion:JDA:6.2.0") {
        exclude(module = "opus-java")
        exclude(module = "kotlin-stdlib")
        exclude(module = "slf4j-nop")
        exclude(module="tink")
    }


}

val targetJavaVersion = 8
kotlin {
    jvmToolchain(targetJavaVersion)

}
tasks.shadowJar {
    archiveFileName.set("${project.name}-${archiveVersion.get()}.jar")
    archiveClassifier.set("shaded")

    minimize {
        // Exclude specific runtime dependencies
        exclude(dependency("org.slf4j:slf4j-api:.*"))
        exclude(dependency("org.slf4j:slf4j-simple:.*"))
    }



    // Merge service loaders
    mergeServiceFiles()
    // Clean META-INF
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/maven/**")
    exclude("META-INF/proguard/**")
}


tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
// Task to copy the jar to the server plugins folder
tasks.register<Copy>("copyToServerPlugins") {
    dependsOn("shadowJar")  // Ensure shadowJar completes before copying
    from(tasks.shadowJar.get().outputs.files.singleFile)
    into("E:/Minecraft/servers/Development/PaperMC-1.21.10/plugins")
}

// Combined task to build and copy
tasks.register("buildAndCopy") {
    dependsOn("build", "copyToServerPlugins")
}