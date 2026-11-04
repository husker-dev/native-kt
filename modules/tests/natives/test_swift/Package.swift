// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "TestSwift",
    products: [
        .library(name: "TestSwiftStatic", type: .static, targets: ["TestSwift"]),
        .library(name: "TestSwiftDynamic", type: .dynamic, targets: ["TestSwift"]),
    ],
    dependencies: [
        .package(path: "../../../cli/build/nativekt-swift"),
    ],
    targets: [
        .target(
            name: "TestSwift",
            plugins: [.plugin(name: "NativeKtPlugin", package: "nativekt-swift")]
        ),
    ]
)
