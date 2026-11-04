func criticalPrimitives(
    a1: UInt16, a2: Bool, a3: Int8, a4: UInt8,
    a5: Int16, a6: UInt16, a7: Int32, a8: UInt32,
    a9: Int64, a10: UInt64, a11: Float, a12: Double
) -> Bool {
    a1 == 97 && a2 && a3 == 1 && a4 == 255
        && a5 == 3 && a6 == 65535 && a7 == 5 && a8 == 4294967295
        && a9 == 7 && a10 == 18446744073709551615
        && floatEquals(a11, 1.0) && doubleEquals(a12, 2.0)
}

func criticalEnum(a1: MyEnum) -> Bool { a1 == .case1 }
func criticalString(a1: String) -> Bool { a1 == "test string" }
func criticalStringEmpty(a1: String) -> Bool { a1.isEmpty }
func criticalStringN(a1: String?) -> Bool { a1 == nil }
func criticalInterface(a1: MyInterface) -> Bool { a1.test() }
func criticalInterfaceN(a1: MyInterface?) -> Bool { a1 == nil }

func criticalPrimitivesArray(
    a1: [UInt16], a2: [Bool], a3: [Int8], a4: [UInt8],
    a5: [Int16], a6: [UInt16], a7: [Int32], a8: [UInt32],
    a9: [Int64], a10: [UInt64], a11: [Float], a12: [Double]
) -> Bool {
    passCharArray(arg: a1) && passBooleanArray(arg: a2)
        && passByteArray(arg: a3) && passUbyteArray(arg: a4)
        && passShortArray(arg: a5) && passUshortArray(arg: a6)
        && passIntArray(arg: a7) && passUintArray(arg: a8)
        && passLongArray(arg: a9) && passUlongArray(arg: a10)
        && passFloatArray(arg: a11) && passDoubleArray(arg: a12)
}

func criticalEnumArray(a1: [MyEnum]) -> Bool { a1 == [.case1, .case2] }

func criticalPrimitivesArrayEmpty(
    a1: [UInt16], a2: [Bool], a3: [Int8], a4: [UInt8],
    a5: [Int16], a6: [UInt16], a7: [Int32], a8: [UInt32],
    a9: [Int64], a10: [UInt64], a11: [Float], a12: [Double]
) -> Bool {
    a1.isEmpty && a2.isEmpty && a3.isEmpty && a4.isEmpty
        && a5.isEmpty && a6.isEmpty && a7.isEmpty && a8.isEmpty
        && a9.isEmpty && a10.isEmpty && a11.isEmpty && a12.isEmpty
}

func criticalEnumArrayEmpty(a1: [MyEnum]) -> Bool { a1.isEmpty }

func criticalPrimitivesArrayN(
    a1: [UInt16]?, a2: [Bool]?, a3: [Int8]?, a4: [UInt8]?,
    a5: [Int16]?, a6: [UInt16]?, a7: [Int32]?, a8: [UInt32]?,
    a9: [Int64]?, a10: [UInt64]?, a11: [Float]?, a12: [Double]?
) -> Bool {
    a1 == nil && a2 == nil && a3 == nil && a4 == nil
        && a5 == nil && a6 == nil && a7 == nil && a8 == nil
        && a9 == nil && a10 == nil && a11 == nil && a12 == nil
}

func criticalEnumArrayN(a1: [MyEnum]?) -> Bool { a1 == nil }

func criticalReturnChar() -> UInt16 { 97 }
func criticalReturnBoolean() -> Bool { true }
func criticalReturnByte() -> Int8 { 1 }
func criticalReturnUbyte() -> UInt8 { 255 }
func criticalReturnShort() -> Int16 { 1 }
func criticalReturnUshort() -> UInt16 { 65535 }
func criticalReturnInt() -> Int32 { 1 }
func criticalReturnUint() -> UInt32 { 4294967295 }
func criticalReturnLong() -> Int64 { 1 }
func criticalReturnUlong() -> UInt64 { 18446744073709551615 }
func criticalReturnFloat() -> Float { 1.0 }
func criticalReturnDouble() -> Double { 1.0 }
func criticalReturnEnum() -> MyEnum { .case1 }
func criticalReturnInterface() -> MyInterface { MyInterface() }
