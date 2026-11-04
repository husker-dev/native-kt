func floatEquals(_ lhs: Float, _ rhs: Float) -> Bool {
    abs(lhs - rhs) < 0.00001
}

func doubleEquals(_ lhs: Double, _ rhs: Double) -> Bool {
    abs(lhs - rhs) < 0.00001
}

// MARK: - Arguments

func passVoid() -> Bool { true }
func passChar(arg: UInt16) -> Bool { arg == 97 }
func passCharN(arg: UInt16?) -> Bool { arg == nil || arg == 97 }
func passBoolean(arg: Bool) -> Bool { arg }
func passBooleanN(arg: Bool?) -> Bool { arg ?? true }
func passByte(arg: Int8) -> Bool { arg == 1 }
func passByteN(arg: Int8?) -> Bool { arg == nil || arg == 1 }
func passUbyte(arg: UInt8) -> Bool { arg == 255 }
func passUbyteN(arg: UInt8?) -> Bool { arg == nil || arg == 255 }
func passShort(arg: Int16) -> Bool { arg == 1 }
func passShortN(arg: Int16?) -> Bool { arg == nil || arg == 1 }
func passUshort(arg: UInt16) -> Bool { arg == 65535 }
func passUshortN(arg: UInt16?) -> Bool { arg == nil || arg == 65535 }
func passInt(arg: Int32) -> Bool { arg == 99 }
func passIntN(arg: Int32?) -> Bool { arg == nil || arg == 99 }
func passUint(arg: UInt32) -> Bool { arg == 4294967295 }
func passUintN(arg: UInt32?) -> Bool { arg == nil || arg == 4294967295 }
func passLong(arg: Int64) -> Bool { arg == 9223372036854775805 }
func passLongN(arg: Int64?) -> Bool { arg == nil || arg == 9223372036854775805 }
func passUlong(arg: UInt64) -> Bool { arg == 18446744073709551615 }
func passUlongN(arg: UInt64?) -> Bool { arg == nil || arg == 18446744073709551615 }
func passFloat(arg: Float) -> Bool { floatEquals(arg, 99.9) }
func passFloatN(arg: Float?) -> Bool { arg == nil || arg == 99.9 }
func passDouble(arg: Double) -> Bool { arg == 1.1 }
func passDoubleN(arg: Double?) -> Bool { arg == nil || arg == 1.1 }
func passString(arg: String) -> Bool { arg == "test string" }
func passStringEmpty(arg: String) -> Bool { arg.isEmpty }
func passStringN(arg: String?) -> Bool { arg == nil }
func passEnum(arg: MyEnum) -> Bool { arg == .case2 }
func passEnumN(arg: MyEnum?) -> Bool { arg == nil || arg == .case2 }

// MARK: - Return values

func returnVoid() {}
func returnChar() -> UInt16 { 97 }
func returnCharN() -> UInt16? { nil }
func returnBoolean() -> Bool { true }
func returnBooleanN() -> Bool? { true }
func returnByte() -> Int8 { 99 }
func returnByteN() -> Int8? { nil }
func returnUbyte() -> UInt8 { 255 }
func returnUbyteN() -> UInt8? { 255 }
func returnShort() -> Int16 { 99 }
func returnShortN() -> Int16? { nil }
func returnUshort() -> UInt16 { 65535 }
func returnUshortN() -> UInt16? { 65535 }
func returnInt() -> Int32 { 99 }
func returnIntN() -> Int32? { nil }
func returnUint() -> UInt32 { 4294967295 }
func returnUintN() -> UInt32? { 4294967295 }
func returnLong() -> Int64 { 9223372036854775805 }
func returnLongN() -> Int64? { nil }
func returnUlong() -> UInt64 { 18446744073709551615 }
func returnUlongN() -> UInt64? { 18446744073709551615 }
func returnFloat() -> Float { 99.0 }
func returnFloatN() -> Float? { nil }
func returnDouble() -> Double { 99.0 }
func returnDoubleN() -> Double? { 99.0 }
func returnString() -> String { "test string" }
func returnStringEmpty() -> String { "" }
func returnStringN() -> String? { nil }
func returnEnum() -> MyEnum { .case2 }
func returnEnumN() -> MyEnum? { .case2 }

// MARK: - Round trips

func pingChar(arg: UInt16) -> UInt16 { arg }
func pingBoolean(arg: Bool) -> Bool { arg }
func pingByte(arg: Int8) -> Int8 { arg }
func pingUbyte(arg: UInt8) -> UInt8 { arg }
func pingShort(arg: Int16) -> Int16 { arg }
func pingUshort(arg: UInt16) -> UInt16 { arg }
func pingInt(arg: Int32) -> Int32 { arg }
func pingUint(arg: UInt32) -> UInt32 { arg }
func pingLong(arg: Int64) -> Int64 { arg }
func pingUlong(arg: UInt64) -> UInt64 { arg }
func pingFloat(arg: Float) -> Float { arg }
func pingDouble(arg: Double) -> Double { arg }
func pingString(arg: String) -> String { arg }
func pingStringEmpty(arg: String) -> String { arg }
func pingStringN(arg: String?) -> String? { arg }
func pingEnum(arg: MyEnum) -> MyEnum { arg }
