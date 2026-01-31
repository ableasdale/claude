plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("dev.langchain4j:langchain4j:1.10.0")
    implementation("dev.langchain4j:langchain4j-open-ai:1.10.0")
    implementation("dev.langchain4j:langchain4j-ollama:1.10.0")
}

tasks.test {
    useJUnitPlatform()
}