func passDictionary(arg: MyDictionary) -> Bool {
    arg.a == 1 && arg.b == 2 && arg.c == 3 && arg.d == 4
}

func passDictionaryN(arg: MyDictionary?) -> Bool { arg == nil }
func returnDictionary() -> MyDictionary { MyDictionary(a: 1, b: 2, c: 3, d: 4) }
func returnDictionaryN() -> MyDictionary? { nil }
func pingDictionary(arg: MyDictionary) -> MyDictionary { arg }
func pingEmptyDictionary(arg: EmptyDictionary) -> EmptyDictionary { arg }
func pingDictionaryN(arg: MyDictionary?) -> MyDictionary? { arg }

func passBigDictionary(arg: TypeDictionary) -> Bool {
    // a19 (callback) is intentionally not invoked, matching the other languages.
    let primitives = arg.a1 == 97 && arg.a2
        && arg.a3 == 123 && arg.a4 == 123
        && arg.a5 == 123 && arg.a6 == 123
        && arg.a7 == 123 && arg.a8 == 123
        && arg.a9 == 9223372036854775807 && arg.a10 == 9223372036854775807
        && arg.a11 == 123.0 && arg.a12 == 123.4

    let objects = arg.a13 == "test string" && arg.a14 == .case2
        && passDictionary(arg: arg.a15) && arg.a16 == nil
        && arg.a17.test() && arg.a18 == nil

    let primitiveArrays = arg.a20 == [97, 98] && arg.a21 == [true, false]
        && arg.a22 == [1, 2] && arg.a23 == [1, 2]
        && arg.a24 == [1, 2] && arg.a25 == [1, 2]
        && arg.a26 == [1, 2] && arg.a27 == [1, 2]
        && arg.a28 == [1, 2] && arg.a29 == [1, 2]
        && arg.a30 == [1.2, 3.4] && arg.a31 == [1.2, 3.4]

    let objectArrays = passStringArray(arg: arg.a32)
        && passEnumArray(arg: arg.a33)
        && passDictionaryArray(arg: arg.a34)
        && passInterfaceArray(arg: arg.a35)

    return primitives && objects && primitiveArrays && objectArrays
}

func returnBigDictionary(callback: VoidCallback, inter: MyInterface) -> TypeDictionary {
    TypeDictionary(
        a1: 97,
        a2: true,
        a3: 123,
        a4: 123,
        a5: 123,
        a6: 123,
        a7: 123,
        a8: 123,
        a9: 9223372036854775807,
        a10: 9223372036854775807,
        a11: 123.0,
        a12: 123.4,
        a13: "test string",
        a14: .case2,
        a15: returnDictionary(),
        a16: nil,
        a17: inter,
        a18: nil,
        a19: callback,
        a20: [97, 98],
        a21: [true, false],
        a22: [1, 2],
        a23: [1, 2],
        a24: [1, 2],
        a25: [1, 2],
        a26: [1, 2],
        a27: [1, 2],
        a28: [1, 2],
        a29: [1, 2],
        a30: [1.2, 3.4],
        a31: [1.2, 3.4],
        a32: ["string1", "string2"],
        a33: [.case1, .case2],
        a34: returnDictionaryArray(),
        a35: [inter, inter]
    )
}

func pingBigDictionary(arg: TypeDictionary) -> TypeDictionary { arg }
func passBigDictionaryN(arg: TypeDictionary?) -> Bool { arg == nil }
func returnBigDictionaryN() -> TypeDictionary? { nil }
func pingBigDictionaryN(arg: TypeDictionary?) -> TypeDictionary? { arg }
