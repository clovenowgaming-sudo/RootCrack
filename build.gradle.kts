plugins {
    alias(libs.plugins.agp.app) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

// app/src/main/cpp/kp_abi_version is the single source of the KernelPatch
// release this manager targets. apd/build.rs mirrors it.
project.ext.set("kernelPatchVersion", getKernelPatchVersion())

extra.set("androidMinSdkVersion", 26)
extra.set("androidTargetSdkVersion", 35)
extra.set("androidCompileSdkVersion", 35)
extra.set("managerVersionCode", getVersionCode())
extra.set("managerVersionName", getVersionName())

fun Project.exec(command: String) = providers.exec {
    commandLine(command.split(" "))
}.standardOutput.asText.get().trim()

fun getGitCommitCount(): Int = try {
    exec("git rev-list --count HEAD").toInt()
} catch (e: Exception) { 0 }

fun getGitDescribe(): String = try {
    exec("git rev-parse --verify --short HEAD")
} catch (e: Exception) { "dev" }

fun getVersionCode(): Int {
    val props = java.util.Properties().apply {
        File(rootDir, "version.properties").inputStream().use { load(it) }
    }
    val epoch = props.getProperty("rootCrackVersionEpoch").toInt()
    return epoch + getGitCommitCount()
}

fun getKernelPatchVersion(): String {
    val f = File(rootDir, "app/src/main/cpp/kp_abi_version")
    if (!f.exists()) return "0.13.9"
    val header = f.readText()
    fun part(name: String) = Regex("""#define $name (\d+)""")
        .find(header)?.groupValues?.get(1) ?: error("$name not found")
    return "${part("MAJOR")}.${part("MINOR")}.${part("PATCH")}"
}

fun getVersionName(): String = getGitDescribe()

tasks.register("printVersion") {
    doLast {
        println("Version code: ${project.extra["managerVersionCode"]}")
        println("Version name: ${project.extra["managerVersionName"]}")
        println("KernelPatch ABI target: ${project.extra["kernelPatchVersion"]}")
    }
}
