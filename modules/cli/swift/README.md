# native-kt-swift

Swift implementation of native-kt generator.

### Usage

```swift
// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "MyExampleProject",
    products: [
        .library(name: "MyLibStatic", type: .static, targets: ["MyLib"]),
        .library(name: "MyLibDynamic", type: .dynamic, targets: ["MyLib"]),
    ],
    dependencies: [
        .package(url: "https://github.com/husker-dev/native-kt-swift.git",
                 from: "${{VERSION}}")
    ],
    targets: [
        .target(
            name: "MyLib",
            plugins: [
                .plugin(name: "NativeKtPlugin", package: "nativekt-swift")
            ]
        ),
    ]
)

```