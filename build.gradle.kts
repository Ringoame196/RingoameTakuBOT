plugins {
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jmailen.kotlinter") version "3.8.0"
}

group = "com.github.ringoame196"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // Discord JDA
    implementation("net.dv8tion:JDA:5.0.1")

    // Utility
    implementation("net.sf.trove4j:trove4j:3.0.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("org.yaml:snakeyaml:2.2")

    // Notion / HTTP
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.google.code.gson:gson:2.10")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("") // `-all` を付けない
    manifest {
        attributes["Main-Class"] = "com.github.ringoame196.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.register<Copy>("buildServer") {
    dependsOn("shadowJar")
    from(tasks.shadowJar.get().archiveFile) // ShadowJar の成果物をコピー
    into("Z:/discord/ringoametakuBOT") // コピー先
    rename { "bot.jar" } // 名前を固定
}

kotlin {
    jvmToolchain(17)
}
