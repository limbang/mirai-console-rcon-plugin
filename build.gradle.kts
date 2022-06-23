plugins {
    val kotlinVersion = "1.7.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.12.0-RC"
}

group = "top.limbang"
version = "0.1.1"

mirai {
    coreVersion = "2.11.1"
    consoleVersion = "2.11.1"
}

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    compileOnly("top.limbang:mirai-plugin-general-interface:1.0.1")
    testImplementation(kotlin("test"))
}