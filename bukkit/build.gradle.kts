plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

dependencies {
    implementation(project(":common"))
    implementation("io.nexstudios.itemservice:bukkit:v1.0.0")

    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    implementation("io.nexstudios:framework-paper:v1.0.2")
    implementation("io.nexstudios.menuservice:bukkit:v1.0.1")
    implementation("io.nexstudios.databaseservice:bukkit:v1.0.0")
    implementation("io.nexstudios.configservice:platform:v1.0.0")
    implementation("io.nexstudios.languageservice:bukkit:v1.0.0")
    implementation("io.nexstudios.commandservice:bukkit:v1.0.0")


    implementation("org.yaml:snakeyaml:2.2")
}

tasks.processResources {
    filteringCharset = "UTF-8"
}

tasks.shadowJar {
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    relocate("org.yaml.snakeyaml", "io.nexstudios.nexlogic.libs.snakeyaml")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}