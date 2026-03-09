import org.jetbrains.compose. desktop.application.dsl. TargetFormat
import org.jetbrains.kotlin.gradle. dsl.JvmTarget
import org.jetbrains.kotlin. gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvmToolchain(21)

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle. viewmodelCompose)
            implementation(libs.androidx.lifecycle. runtimeCompose)
            implementation(compose.materialIconsExtended)
            implementation(compose.material3AdaptiveNavigationSuite)
            // HTTP + JSON
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
            implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
            implementation("org.jetbrains.kotlin:kotlin-reflect:${libs.versions.kotlin.get()}")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    compilerOptions.freeCompilerArgs.addAll(
        listOf(
            "-Xadd-modules=java.smartcardio"
        )
    )
}

compose.desktop {
    application {
        mainClass = "org.example.project.MainUserKt" // ✅ Đổi thành MainUserKt

        jvmArgs += listOf("--add-modules", "java.smartcardio")

        nativeDistributions {
            targetFormats(TargetFormat. Dmg, TargetFormat. Msi, TargetFormat.Deb)
            packageName = "org.example.project"
            packageVersion = "1.0.0"
            modules("java.smartcardio")
        }
    }
}

// ✅ THÊM 2 TASKS NÀY VÀO CUỐI FILE

// ✅ THAY 2 TASKS NÀY (từ dòng 66)

tasks.register<JavaExec>("runUser") {
    group = "application"
    description = "Run User Application"
    mainClass.set("org.example.project.MainUserKt")

    classpath = files(
        tasks.named("jvmJar").get().outputs.files,
        configurations.named("jvmRuntimeClasspath").get()
    )

    jvmArgs("--add-modules", "java. smartcardio")

    dependsOn("jvmJar")
}

tasks.register<JavaExec>("runAdmin") {
    group = "application"
    description = "Run Admin Application"
    mainClass.set("org.example.project.MainAdminKt")

    classpath = files(
        tasks.named("jvmJar").get().outputs.files,
        configurations.named("jvmRuntimeClasspath").get()
    )

    jvmArgs("--add-modules", "java.smartcardio")

    dependsOn("jvmJar")
}