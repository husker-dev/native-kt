@file:Suppress("CanSealedSubClassBeObject")

package com.huskerdev.nativekt

import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.parsers.TomlParser
import com.akuleshov7.ktoml.tree.nodes.TomlKeyValueArray
import com.akuleshov7.ktoml.tree.nodes.TomlKeyValuePrimitive
import com.akuleshov7.ktoml.tree.nodes.pairs.values.TomlBasicString
import com.huskerdev.stdlibx.fs.SyncPath
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class NativeKtModule(
    var name: String,
    var classPath: String,
    var apiFile: SyncPath,
    var useCoroutines: Boolean,

    var commonTarget: String,
    var targets: List<String>,
    var stubs: List<String>,
    var buildSystem: BuildSystem,

    val jvm: Jvm,
    val js: Js,
    val android: Android,

    var debug: List<DebugKind>,
) {
    val expectActual: Boolean
        get() = targets.size != 1 && commonTarget != targets[0]

    @Serializable
    data class Jvm(
        var useJvmRecord: Boolean,

        var useJNI: Boolean,
        var useForeignApi: Boolean,
        var useJVMCI: Boolean
    )

    @Serializable
    data class Js(
        var useJsBigInt: Boolean,
        var target: JsTarget
    )

    @Serializable
    data class Android(
        var ndkVersion: String?,
        var targets: List<String>,
        var useCriticalNative: Boolean
    )
}

// ==============
//  Build system
// ==============

@Serializable
sealed interface BuildSystem {
    val language: Language
    var projectDir: SyncPath

    @Serializable
    @SerialName("cmake")
    data class CMake(
        override var projectDir: SyncPath,
        override val language: Language,
        var buildType: BuildType = BuildType.RELEASE,
        var args: List<String> = emptyList(),
        var headerFile: SyncPath = projectDir.resolve("include/header/api.${language.headerExtension}"),
    ): BuildSystem {
        enum class BuildType(val cmakeName: String) {
            DEBUG("Debug"),
            REL_WITH_DEB_INFO("RelWithDebInfo"),
            RELEASE("Release"),
            MIN_SIZE_REL("MinSizeRel")
        }
    }

    @Serializable
    @SerialName("cargo")
    data class Cargo(
        override var projectDir: SyncPath,
        var buildType: BuildType = BuildType.RELEASE
    ): BuildSystem {
        override val language: Language = Language.RUST

        enum class BuildType(val cargoName: String) {
            RELEASE("release"),
            DEBUG("debug")
        }
    }
}

@Suppress("unused")
enum class Language(
    val sourceExtension: String? = null,
    val headerExtension: String? = null
) {
    C("c", "h"),
    CPP("cpp", "hpp"),
    RUST
}

@Suppress("unused")
enum class JsTarget {
    WEB,
    NODE
}

// ==============
//     Debug
// ==============

@Suppress("unused")
enum class DebugKind {
    PRINT_EXEC,
    PRINT_LINKER_OPTIONS
}

// ==============
//    Parsing
// ==============
@Suppress("UNCHECKED_CAST")
fun parseModuleFromToml(path: SyncPath): NativeKtModule =
    parseModuleFromToml(path.readString(), path.absolute().parent!!)


@Suppress("UNCHECKED_CAST")
fun parseModuleFromToml(text: String, relativePath: SyncPath): NativeKtModule {
    val ast = TomlParser(TomlInputConfig()).parseString(text)

    val values = ast.children.associate { table ->
        table.name to table.children.associate { child ->
            when (child) {
                is TomlKeyValuePrimitive -> child.key.toString() to child.value.content.toString()
                is TomlKeyValueArray -> child.key.toString() to (child.value.content as List<*>).map {
                    (it as TomlBasicString).content.toString()
                }
                else -> throw UnsupportedOperationException("Unexpected element: $child")
            }
        }
    }
    fun string(block: String, key: String) =
        values[block]?.get(key) as String?
    fun stringRequired(block: String, key: String) =
        string(block, key) ?: throw NullPointerException("$block.${key} is a required field.")
    fun stringArray(block: String, key: String) =
        values[block]?.get(key) as List<String>?
    fun bool(block: String, key: String) =
        string(block, key)?.let { it == "true" }

    return NativeKtModule(
        name = stringRequired("description", "name"),
        classPath = stringRequired("kotlin", "package"),
        apiFile = relativePath.resolve(stringRequired("description", "api_file")).normalize(),
        useCoroutines = bool("kotlin", "use_coroutines") ?: false,
        commonTarget = string("compilation", "common_target") ?: "common",
        targets = stringArray("compilation", "targets") ?: listOf(
            "jvm", "android",
            "js", "wasmJs",
            "mingwX64",
            "macosArm64", "macosX64",
            "linuxArm64", "linuxX64",
            "iosArm64", "iosX64", "iosSimulatorArm64",
            "watchosX64", "watchosArm32", "watchosArm64", "watchosDeviceArm64", "watchosSimulatorArm64",
            "tvosX64", "tvosArm64", "tvosSimulatorArm64",
            "androidNativeX64", "androidNativeX86", "androidNativeArm32", "androidNativeArm64"
        ),
        stubs = stringArray("compilation", "stubs") ?: emptyList(),
        buildSystem = when {
            "cargo" in values -> BuildSystem.Cargo(
                projectDir = relativePath.resolve(stringRequired("cargo", "project_dir")).normalize(),
                buildType = when(string("cargo", "build_type")) {
                    "release", null -> BuildSystem.Cargo.BuildType.RELEASE
                    "debug" -> BuildSystem.Cargo.BuildType.DEBUG
                    else -> throw NullPointerException("Unknown Cargo build type")
                },
            )
            "cmake" in values -> BuildSystem.CMake(
                projectDir = relativePath.resolve(stringRequired("cmake", "project_dir")).normalize(),
                headerFile = relativePath.resolve(stringRequired("cmake", "header_file")).normalize(),
                buildType = when(string("cmake", "build_type")) {
                    "Release", null -> BuildSystem.CMake.BuildType.RELEASE
                    "Debug" -> BuildSystem.CMake.BuildType.DEBUG
                    "RelWithDebInfo" -> BuildSystem.CMake.BuildType.REL_WITH_DEB_INFO
                    "MinSizeRel" -> BuildSystem.CMake.BuildType.MIN_SIZE_REL
                    else -> throw NullPointerException("Unknown CMake build type")
                },
                args = stringArray("cmake", "args") ?: emptyList(),
                language = when(val value = stringRequired("cmake", "language")) {
                    "c" -> Language.C
                    "cpp", "c++" -> Language.CPP
                    else -> throw UnsupportedOperationException("Unknown CMake language: $value")
                }
            )
            else -> throw NullPointerException("Build system is not specified")
        },
        jvm = NativeKtModule.Jvm(
            useJvmRecord = bool("jvm", "use_records") ?: false,
            useJNI = stringArray("jvm", "backends")?.contains("jni") ?: true,
            useForeignApi = stringArray("jvm", "backends")?.contains("foreign") ?: true,
            useJVMCI = stringArray("jvm", "backends")?.contains("jvmci") ?: true
        ),
        js = NativeKtModule.Js(
            useJsBigInt = bool("js", "use_bigint") ?: false,
            target = when(string("js", "target")) {
                "web" -> JsTarget.WEB
                "node", "nodejs" -> JsTarget.NODE
                null -> JsTarget.WEB
                else -> throw UnsupportedOperationException("Unknown JS target")
            }
        ),
        android = NativeKtModule.Android(
            ndkVersion = when(val value = string("android", "ndk_version")) {
                "latest" -> null
                else -> value
            },
            targets = stringArray("android", "targets") ?:
                listOf("arm64-v8a", "armeabi-v7a", "x86_64"),
            useCriticalNative = bool("android", "critical_natives") ?: true
        ),
        debug = emptyList()
    )
}