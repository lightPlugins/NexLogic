plugins {
    id("java")
    id("io.freefair.lombok") version "8.13.1" apply false
}

allprojects {
    group = "io.nexstudios.nexlogic"
    version = "v1.0.0"

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://mvn.lumine.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://repo.nexomc.com/releases")
        maven("https://repo.glaremasters.me/repository/towny/")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.freefair.lombok")

    dependencies {
        compileOnly("org.jetbrains:annotations:26.0.2")
        implementation("com.ezylang:EvalEx:3.6.0")
        testCompileOnly("org.jetbrains:annotations:26.0.2")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}
