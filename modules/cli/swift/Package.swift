// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "NativeKtSwift",
    products: [
        .plugin(name: "NativeKtPlugin", targets: ["NativeKtPlugin"]),
    ],
    targets: [
        .binaryTarget(name: "NativeKtCli", path: "NativeKtCli.artifactbundle"),
        .plugin(
            name: "NativeKtPlugin",
            capability: .buildTool(),
            dependencies: ["NativeKtCli"]
        ),
    ]
)
