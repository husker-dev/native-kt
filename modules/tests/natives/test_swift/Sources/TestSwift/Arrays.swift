// MARK: - Arguments

func passCharArray(arg: [UInt16]) -> Bool { arg == [97, 98] }
func passCharArrayEmpty(arg: [UInt16]) -> Bool { arg.isEmpty }
func passCharArrayN(arg: [UInt16]?) -> Bool { arg == nil }
func passCharNArray(arg: [UInt16?]) -> Bool { arg == [nil, 97] }
func passBooleanArray(arg: [Bool]) -> Bool { arg == [true, false] }
func passBooleanNArray(arg: [Bool?]) -> Bool { arg == [nil, true] }
func passByteArray(arg: [Int8]) -> Bool { arg == [1, 2] }
func passByteNArray(arg: [Int8?]) -> Bool { arg == [nil, 1] }
func passUbyteArray(arg: [UInt8]) -> Bool { arg == [1, 255] }
func passUbyteNArray(arg: [UInt8?]) -> Bool { arg == [nil, 255] }
func passShortArray(arg: [Int16]) -> Bool { arg == [1, 2] }
func passShortNArray(arg: [Int16?]) -> Bool { arg == [nil, 1] }
func passUshortArray(arg: [UInt16]) -> Bool { arg == [1, 65535] }
func passUshortNArray(arg: [UInt16?]) -> Bool { arg == [nil, 65535] }
func passIntArray(arg: [Int32]) -> Bool { arg == [1, 2] }
func passIntNArray(arg: [Int32?]) -> Bool { arg == [nil, 1] }
func passUintArray(arg: [UInt32]) -> Bool { arg == [1, 4294967295] }
func passUintNArray(arg: [UInt32?]) -> Bool { arg == [nil, 4294967295] }
func passLongArray(arg: [Int64]) -> Bool { arg == [1, 2] }
func passLongNArray(arg: [Int64?]) -> Bool { arg == [nil, 1] }
func passUlongArray(arg: [UInt64]) -> Bool { arg == [1, 18446744073709551615] }
func passUlongNArray(arg: [UInt64?]) -> Bool { arg == [nil, 18446744073709551615] }
func passFloatArray(arg: [Float]) -> Bool { arg == [1.1, 2.2] }
func passFloatNArray(arg: [Float?]) -> Bool { arg == [nil, 1.1] }
func passDoubleArray(arg: [Double]) -> Bool { arg == [1.1, 2.2] }
func passDoubleNArray(arg: [Double?]) -> Bool { arg == [nil, 1.1] }
func passStringArray(arg: [String]) -> Bool { arg == ["string1", "string2"] }
func passStringArrayN(arg: [String?]) -> Bool { arg == [nil, nil] }
func passEnumArray(arg: [MyEnum]) -> Bool { arg == [.case1, .case2] }
func passEnumNArray(arg: [MyEnum?]) -> Bool { arg == [nil, .case2] }

func passDictionaryArray(arg: [MyDictionary]) -> Bool {
    arg.count == 2 && passDictionary(arg: arg[0])
        && arg[1].a == 5 && arg[1].b == 6 && arg[1].c == 7 && arg[1].d == 8
}

func passDictionaryArrayN(arg: [MyDictionary?]) -> Bool {
    arg.count == 2 && arg[0] == nil && arg[1] == nil
}

func passInterfaceArray(arg: [MyInterface]) -> Bool {
    arg.count == 2 && arg[0].test() && arg[1].test()
}

func passInterfaceArrayN(arg: [MyInterface?]) -> Bool {
    arg.count == 2 && arg[0] == nil && arg[1] == nil
}

// MARK: - Return values

func returnCharArray() -> [UInt16] { [97, 98] }
func returnCharArrayEmpty() -> [UInt16] { [] }
func returnCharArrayN() -> [UInt16]? { nil }
func returnCharNArray() -> [UInt16?] { [nil, 97] }
func returnBooleanArray() -> [Bool] { [true, false] }
func returnBooleanNArray() -> [Bool?] { [nil, true] }
func returnByteArray() -> [Int8] { [1, 2] }
func returnByteNArray() -> [Int8?] { [nil, 1] }
func returnUbyteArray() -> [UInt8] { [1, 255] }
func returnUbyteNArray() -> [UInt8?] { [nil, 255] }
func returnShortArray() -> [Int16] { [1, 2] }
func returnShortNArray() -> [Int16?] { [nil, 1] }
func returnUshortArray() -> [UInt16] { [1, 65535] }
func returnUshortNArray() -> [UInt16?] { [nil, 65535] }
func returnIntArray() -> [Int32] { [1, 2] }
func returnIntNArray() -> [Int32?] { [nil, 1] }
func returnUintArray() -> [UInt32] { [1, 4294967295] }
func returnUintNArray() -> [UInt32?] { [nil, 4294967295] }
func returnLongArray() -> [Int64] { [1, 2] }
func returnLongNArray() -> [Int64?] { [nil, 1] }
func returnUlongArray() -> [UInt64] { [1, 18446744073709551615] }
func returnUlongNArray() -> [UInt64?] { [nil, 18446744073709551615] }
func returnFloatArray() -> [Float] { [1.1, 2.2] }
func returnFloatNArray() -> [Float?] { [nil, 1.1] }
func returnDoubleArray() -> [Double] { [1.1, 2.2] }
func returnDoubleNArray() -> [Double?] { [nil, 1.1] }
func returnStringArray() -> [String] { ["string1", "string2"] }
func returnStringArrayN() -> [String?] { [nil, nil] }
func returnEnumArray() -> [MyEnum] { [.case1, .case2] }
func returnEnumNArray() -> [MyEnum?] { [nil, .case2] }

func returnDictionaryArray() -> [MyDictionary] {
    [MyDictionary(a: 1, b: 2, c: 3, d: 4), MyDictionary(a: 5, b: 6, c: 7, d: 8)]
}

func returnDictionaryArrayN() -> [MyDictionary?] { [nil, nil] }

// MARK: - Round trips

func pingCharArray(arg: [UInt16]) -> [UInt16] { arg }
func pingCharArrayEmpty(arg: [UInt16]) -> [UInt16] { arg }
func pingCharArrayN(arg: [UInt16]?) -> [UInt16]? { arg }
func pingBooleanArray(arg: [Bool]) -> [Bool] { arg }
func pingByteArray(arg: [Int8]) -> [Int8] { arg }
func pingUbyteArray(arg: [UInt8]) -> [UInt8] { arg }
func pingShortArray(arg: [Int16]) -> [Int16] { arg }
func pingUshortArray(arg: [UInt16]) -> [UInt16] { arg }
func pingIntArray(arg: [Int32]) -> [Int32] { arg }
func pingUintArray(arg: [UInt32]) -> [UInt32] { arg }
func pingLongArray(arg: [Int64]) -> [Int64] { arg }
func pingUlongArray(arg: [UInt64]) -> [UInt64] { arg }
func pingFloatArray(arg: [Float]) -> [Float] { arg }
func pingDoubleArray(arg: [Double]) -> [Double] { arg }
func pingStringArray(arg: [String]) -> [String] { arg }
func pingStringArrayN(arg: [String?]) -> [String?] { arg }
func pingEnumArray(arg: [MyEnum]) -> [MyEnum] { arg }
func pingDictionaryArray(arg: [MyDictionary]) -> [MyDictionary] { arg }
func pingDictionaryArrayN(arg: [MyDictionary?]) -> [MyDictionary?] { arg }
func pingInterfaceArray(arg: [MyInterface]) -> [MyInterface] { arg }
func pingInterfaceArrayN(arg: [MyInterface?]) -> [MyInterface?] { arg }
