plugins {
    id("java")
}

dependencies {

    compileOnly("io.nexstudios:framework-paper:v1.0.2")

    testImplementation("io.nexstudios:framework-paper:v1.0.2")
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}