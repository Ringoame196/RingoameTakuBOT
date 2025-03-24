plugins {
    kotlin("jvm") version "1.9.23"
}

group = "com.github.ringoame196"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.dv8tion:JDA:5.0.1")
    implementation("net.sf.trove4j:trove4j:3.0.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.slf4j:slf4j-simple:2.0.9") // SLF4J は ログ出力のための統一インターフェース
    implementation("org.yaml:snakeyaml:2.2") // yamlファイル読み込み用
    // notion
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.google.code.gson:gson:2.10")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Copy>("buildServer") {
    from(buildDir.resolve("libs/${project.name}-${project.version}.jar")) // コピー元
    into("Z:/discord/ringoametakuBOT") // コピー先フォルダ
    rename { "bot.jar" } // ファイル名を変更
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.github.ringoame196.MainKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

kotlin {
    jvmToolchain(17)
}