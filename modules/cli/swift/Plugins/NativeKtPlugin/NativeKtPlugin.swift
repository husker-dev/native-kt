import Foundation
import PackagePlugin

@main
struct NativeKtPlugin: BuildToolPlugin {
    func createBuildCommands(context: PluginContext, target: Target) throws -> [Command] {
        guard target is SourceModuleTarget else { return [] }

        let config = context.package.directory.appending("nativekt.toml")
        guard FileManager.default.fileExists(atPath: config.string) else {
            throw PluginError.missingConfig(config.string)
        }

        let output = context.pluginWorkDirectory.appending("GeneratedSources")
        return [
            .prebuildCommand(
                displayName: "Generate Native-Kt Swift bindings for \(target.name)",
                executable: try context.tool(named: "nativekt-cli").path,
                arguments: ["generate", "--config", config.string, "--output", output.string],
                outputFilesDirectory: output
            ),
        ]
    }
}

private enum PluginError: Error {
    case missingConfig(String)
}
