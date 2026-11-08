plugins {
    id("java")
    application
    id("com.gradleup.shadow") version "8.3.6"
}

group = "org.example"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("org.example.documentCLI.cli.DocumentCLI")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Source: https://mvnrepository.com/artifact/info.picocli/picocli
    implementation("info.picocli:picocli:4.7.7")
    implementation("info.picocli:picocli-shell-jline3:4.7.6")
    implementation("org.jline:jline-terminal-jansi:3.26.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.4")
    // Модуль для java.time (в Jackson 3 он встроен, в Jackson 2 — отдельный)
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.4")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    // Убираем суффикс "-all" из имени файла
    archiveClassifier.set("")
    // Убедимся, что манифест содержит правильный Main-Class
    manifest {
        attributes["Main-Class"] = "org.example.documentCLI.cli.DocumentCLI"
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named<JavaExec>("run") {
    jvmArgs("-Dfile.encoding=UTF-8")
}