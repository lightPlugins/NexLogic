plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("maven-publish")
}

dependencies {
    implementation(project(":common"))
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")

    // nex internal module services
    implementation("io.nexstudios.itemservice:bukkit:v1.0.0")
    implementation("io.nexstudios:framework-paper:v1.0.2")
    implementation("io.nexstudios.menuservice:bukkit:v1.0.1")
    implementation("io.nexstudios.databaseservice:bukkit:v1.0.0")
    implementation("io.nexstudios.configservice:platform:v1.0.0")
    implementation("io.nexstudios.languageservice:bukkit:v1.0.0")
    implementation("io.nexstudios.commandservice:bukkit:v1.0.0")

    // third party hooks
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
    compileOnly("com.nexomc:nexo:1.20.1")
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveBaseName.set("NexLogic")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val relocateBase = "io.nexstudios.nexlogic.libs"

    relocate("io.nexstudios.itemservice", "$relocateBase.itemservice")
    relocate("io.nexstudios.framework", "$relocateBase.framework")
    relocate("io.nexstudios.menuservice", "$relocateBase.menuservice")
    relocate("io.nexstudios.databaseservice", "$relocateBase.databaseservice")
    relocate("io.nexstudios.configservice", "$relocateBase.configservice")
    relocate("io.nexstudios.languageservice", "$relocateBase.languageservice")
    relocate("io.nexstudios.commandservice", "$relocateBase.commandservice")

}

tasks.processResources {
    filteringCharset = "UTF-8"
}

tasks.jar {
    enabled = false
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveBaseName.set("NexLogic")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("mavenShadow") {
            groupId = project.group.toString()
            artifactId = "nexlogic-bukkit"
            version = project.version.toString()

            artifact(tasks.shadowJar)
        }
    }
}