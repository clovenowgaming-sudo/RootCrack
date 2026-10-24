import java.net.URI

plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val androidMinSdkVersion: Int = rootProject.extra["androidMinSdkVersion"] as Int
val androidTargetSdkVersion: Int = rootProject.extra["androidTargetSdkVersion"] as Int
val androidCompileSdkVersion: Int = rootProject.extra["androidCompileSdkVersion"] as Int
val managerVersionCode: Int = rootProject.extra["managerVersionCode"] as Int
val managerVersionName: String = rootProject.extra["managerVersionName"] as String
val kernelPatchVersion: String = rootProject.extra["kernelPatchVersion"] as String

android {
    namespace = "com.root.crack"
    compileSdk = androidCompileSdkVersion

    defaultConfig {
        applicationId = "com.root.crack"
        minSdk = androidMinSdkVersion
        targetSdk = androidTargetSdkVersion
        versionCode = managerVersionCode
        versionName = managerVersionName
        ndk.abiFilters.addAll(listOf("arm64-v8a"))
        buildConfigField("String", "KP_ABI_VERSION", "\"$kernelPatchVersion\"")
        externalNativeBuild {
            cmake {
                abiFilters("arm64-v8a")
            }
        }
    }

    signingConfigs {
        create("release") {
            val ksFile = System.getenv("KEYSTORE_FILE")
            if (ksFile != null) {
                storeFile = file(ksFile)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug { isDebuggable = true }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        aidl = true
    }

    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    packaging {
        jniLibs { useLegacyPackaging = true }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.webkit)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.navigation)

    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.libsu.core)
    implementation(libs.libsu.service)
    implementation(libs.libsu.io)

    implementation(libs.kotlinx.coroutines)
    implementation(libs.okhttp)
}

// Downloads the prebuilt kpimg / kptools artifacts from the upstream
// KernelPatch project release matching kp_abi_version. RootCrack drives
// these the same way APatch does: they are the actual kernel-image patcher,
// built and maintained upstream — we do not vendor or reimplement them.
fun registerDownload(taskName: String, url: String, dest: String) {
    tasks.register(taskName) {
        doLast {
            val f = file(dest)
            f.parentFile.mkdirs()
            if (!f.exists()) {
                println(" - downloading $url")
                URI.create(url).toURL().openStream().use { input ->
                    f.outputStream().use { input.copyTo(it) }
                }
            }
        }
    }
}

registerDownload(
    "downloadKpimg",
    "https://github.com/bmax121/KernelPatch/releases/download/$kernelPatchVersion/kpimg-android",
    "${projectDir}/src/main/assets/kpimg",
)
registerDownload(
    "downloadKptools",
    "https://github.com/bmax121/KernelPatch/releases/download/$kernelPatchVersion/kptools-android",
    "${projectDir}/src/main/assets/kptools",
)

tasks.named("preBuild").configure {
    dependsOn("downloadKpimg", "downloadKptools")
}

tasks.register<Exec>("cargoBuildDaemon") {
    executable("cargo")
    args("ndk", "-t", "arm64-v8a", "-o", "${projectDir}/src/main/jniLibs", "build", "--release")
    workingDir("${rootDir}/daemon")
}

tasks.register<Copy>("renameDaemon") {
    dependsOn("cargoBuildDaemon")
    from("${projectDir}/src/main/jniLibs/arm64-v8a") {
        include("rfd")
        rename("rfd", "librfd.so")
    }
    into("${projectDir}/src/main/jniLibs/arm64-v8a")
}

tasks.configureEach {
    if (name == "mergeDebugJniLibFolders" || name == "mergeReleaseJniLibFolders") {
        dependsOn("renameDaemon")
    }
}
