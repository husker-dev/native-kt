func jvmci1() -> Bool { true }
func jvmci2(a1: Int32) -> Bool { a1 == 1 }
func jvmci3(a1: Int32, a2: Int32) -> Bool { a1 == 1 && a2 == 2 }

func jvmci4(
    a1: Int32, a2: Int32, a3: Int32, a4: Int32, a5: Int32,
    a6: Int32, a7: Int32, a8: Int32, a9: Int32
) -> Bool {
    a1 == 1 && a2 == 2 && a3 == 3 && a4 == 4 && a5 == 5
        && a6 == 6 && a7 == 7 && a8 == 8 && a9 == 9
}

func jvmci5(
    a1: Int32, a2: Int64, a3: Int32, a4: Int64, a5: Int32,
    a6: Int64, a7: Int32, a8: Int32, a9: Int64
) -> Bool {
    a1 == 1 && a2 == 2 && a3 == 3 && a4 == 4 && a5 == 5
        && a6 == 6 && a7 == 7 && a8 == 8 && a9 == 9
}

func jvmci6(
    a1: Float, a2: Float, a3: Float, a4: Float, a5: Float,
    a6: Float, a7: Float, a8: Float, a9: Float,
    a10: Int32, a11: Int32, a12: Int32, a13: Int32
) -> Bool {
    floatEquals(a1, 1.0) && floatEquals(a2, 2.0) && floatEquals(a3, 3.0)
        && floatEquals(a4, 4.0) && floatEquals(a5, 5.0) && floatEquals(a6, 6.0)
        && floatEquals(a7, 7.0) && floatEquals(a8, 8.0) && floatEquals(a9, 9.0)
        && a10 == 10 && a11 == 11 && a12 == 12 && a13 == 13
}

func jvmci7(
    a1: Float, a2: Double, a3: Float, a4: Double, a5: Float,
    a6: Double, a7: Float, a8: Float, a9: Double
) -> Bool {
    floatEquals(a1, 1.0) && doubleEquals(a2, 2.0) && floatEquals(a3, 3.0)
        && doubleEquals(a4, 4.0) && floatEquals(a5, 5.0) && doubleEquals(a6, 6.0)
        && floatEquals(a7, 7.0) && floatEquals(a8, 8.0) && doubleEquals(a9, 9.0)
}

func jvmci8(a1: Int32, a2: Double, a3: Float, a4: Int64) -> Bool {
    a1 == 1 && doubleEquals(a2, 2.0) && floatEquals(a3, 3.0) && a4 == 4
}

func jvmci9(
    a1: Int32, a2: Double, a3: Float, a4: Int64, a5: Int64,
    a6: Double, a7: Float, a8: Float, a9: Int32
) -> Bool {
    a1 == 1 && doubleEquals(a2, 2.0) && floatEquals(a3, 3.0)
        && a4 == 4 && a5 == 5 && doubleEquals(a6, 6.0)
        && floatEquals(a7, 7.0) && floatEquals(a8, 8.0) && a9 == 9
}

func jvmci10(
    a1: String, a2: Double, a3: Float, a4: Int64, a5: Int64,
    a6: Double, a7: String, a8: Float, a9: Int32
) -> Bool {
    a1 == "string1" && doubleEquals(a2, 2.0) && floatEquals(a3, 3.0)
        && a4 == 4 && a5 == 5 && doubleEquals(a6, 6.0)
        && a7 == "string7" && floatEquals(a8, 8.0) && a9 == 9
}

func jvmci11(
    a1: Float, a2: Int32, a3: Float, a4: Int32, a5: Float,
    a6: Int32, a7: Float, a8: Int32, a9: Float, a10: Int32,
    a11: Float, a12: Int32, a13: Float, a14: Int32,
    a15: Float, a16: Int32, a17: Float
) -> Bool {
    floatEquals(a1, 1.0) && a2 == 2 && floatEquals(a3, 3.0) && a4 == 4
        && floatEquals(a5, 5.0) && a6 == 6 && floatEquals(a7, 7.0) && a8 == 8
        && floatEquals(a9, 9.0) && a10 == 10 && floatEquals(a11, 11.0) && a12 == 12
        && floatEquals(a13, 13.0) && a14 == 14 && floatEquals(a15, 15.0) && a16 == 16
        && floatEquals(a17, 17.0)
}

func jvmci12() -> Int32 { 1 }
func jvmci13() -> Int64 { 1 }
func jvmci14() -> Float { 1.5 }
func jvmci15() -> Double { 1.5 }
func jvmciArray(array: [Int32]) -> Bool { array == [1, 2, 3] }

func jvmciSomeArrays(array1: [Int32], array2: [Float], array3: [Double]) -> Bool {
    array1 == [1, 2, 3] && array2 == [4.0, 5.0, 6.0] && array3 == [7.0, 8.0, 9.0]
}

func jvmciEnum(enum1: MyEnum, enum2: MyEnum, enumArray: [MyEnum]) -> Bool {
    enum1 == .case1 && enum2 == .case2 && enumArray == [.case1, .case2, .case1]
}
