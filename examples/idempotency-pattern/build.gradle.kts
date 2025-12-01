import io.github.emaarco.bpmn.adapter.GenerateBpmnModelsTask
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.kotlinJpa)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.springframework)
    alias(libs.plugins.springDependency)
    alias(libs.plugins.bpmnToCode)
}

group = "de.emaarco.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.defaultService)
    implementation(libs.bundles.database)
    testImplementation(libs.bundles.test)
}

sourceSets {
    main {
        resources {
            srcDirs("../../configuration")
        }
    }
}

tasks.register<GenerateBpmnModelsTask>("generateBpmnModels") {
    baseDir = "${projectDir}/../../configuration"
    filePattern = "*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "de.emaarco.example.adapter.process"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
    useVersioning = false
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("build") {
    dependsOn("generateBpmnModels")
}
