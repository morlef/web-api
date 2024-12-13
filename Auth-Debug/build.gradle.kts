/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    kotlin("jvm") version "2.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("/lib/api.jar"))
}

group = "si.f5.luna3419"
version = "1.0.0-SNAPSHOT"
description = "Auth-Debug"
java.sourceCompatibility = JavaVersion.VERSION_21

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
