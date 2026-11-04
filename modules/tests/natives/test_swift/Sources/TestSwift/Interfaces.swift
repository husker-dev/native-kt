public final class MyInterface {
    public init() {}
    public init(a: Int32) {}

    public func test() -> Bool {
        print("Hello from Swift interface!")
        return true
    }

    public func testCritical() -> Bool { test() }
    public func passInterface(a1: MyInterface) {}
    public func returnInterface() -> MyInterface { MyInterface() }
    public func snakeCase() {}

    deinit { print("Drop MyInterface") }
}

func passInterface(arg: MyInterface) -> Bool { arg.test() }
func passInterfaceN(arg: MyInterface?) -> Bool { arg == nil }
func pingInterface(arg: MyInterface) -> MyInterface { arg }
func pingInterfaceN(arg: MyInterface?) -> MyInterface? { arg }
