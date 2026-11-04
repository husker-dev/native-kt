// MARK: - Primitive and object arguments

func callbackVoid(arg: VoidCallback) { arg() }
func callbackVoidN(arg: VoidCallback?) -> Bool { arg == nil }
func callbackArgChar(arg: CallbackPassChar) -> Bool { arg(97) }
func callbackArgBoolean(arg: CallbackPassBoolean) -> Bool { arg(true) }
func callbackArgByte(arg: CallbackPassByte) -> Bool { arg(1) }
func callbackArgUbyte(arg: CallbackPassUByte) -> Bool { arg(255) }
func callbackArgShort(arg: CallbackPassShort) -> Bool { arg(1) }
func callbackArgUshort(arg: CallbackPassUShort) -> Bool { arg(65535) }
func callbackArgInt(arg: CallbackPassInt) -> Bool { arg(1) }
func callbackArgUint(arg: CallbackPassUInt) -> Bool { arg(4294967295) }
func callbackArgLong(arg: CallbackPassLong) -> Bool { arg(1) }
func callbackArgUlong(arg: CallbackPassULong) -> Bool { arg(18446744073709551615) }
func callbackArgFloat(arg: CallbackPassFloat) -> Bool { arg(1.1) }
func callbackArgDouble(arg: CallbackPassDouble) -> Bool { arg(1.1) }
func callbackArgString(arg: CallbackPassString) -> Bool { arg("test string") }
func callbackArgStringEmpty(arg: CallbackPassStringEmpty) -> Bool { arg("") }
func callbackArgStringN(arg: CallbackPassStringN) -> Bool { arg(nil) }
func callbackArgCallback(pass: VoidCallback, arg: CallbackPassCallback) -> Bool { arg(pass) }
func callbackArgCallbackN(arg: CallbackPassCallbackN) -> Bool { arg(nil) }
func callbackArgEnum(arg: CallbackPassEnum) -> Bool { arg(.case2) }
func callbackArgDictionary(arg: CallbackPassDictionary) -> Bool { arg(returnDictionary()) }
func callbackArgDictionaryN(arg: CallbackPassDictionaryN) -> Bool { arg(nil) }
func callbackArgInterface(pass: MyInterface, arg: CallbackPassInterface) -> Bool { arg(pass) }
func callbackArgInterfaceN(arg: CallbackPassInterfaceN) -> Bool { arg(nil) }

// MARK: - Primitive and object return values

func callbackReturnChar(arg: CallbackReturnChar) -> Bool { arg() == 97 }
func callbackReturnBoolean(arg: CallbackReturnBoolean) -> Bool { arg() }
func callbackReturnByte(arg: CallbackReturnByte) -> Bool { arg() == 1 }
func callbackReturnUbyte(arg: CallbackReturnUByte) -> Bool { arg() == 255 }
func callbackReturnShort(arg: CallbackReturnShort) -> Bool { arg() == 1 }
func callbackReturnUshort(arg: CallbackReturnUShort) -> Bool { arg() == 65535 }
func callbackReturnInt(arg: CallbackReturnInt) -> Bool { arg() == 1 }
func callbackReturnUint(arg: CallbackReturnUInt) -> Bool { arg() == 4294967295 }
func callbackReturnLong(arg: CallbackReturnLong) -> Bool { arg() == 1 }
func callbackReturnUlong(arg: CallbackReturnULong) -> Bool { arg() == 18446744073709551615 }
func callbackReturnFloat(arg: CallbackReturnFloat) -> Bool { arg() == 1.1 }
func callbackReturnDouble(arg: CallbackReturnDouble) -> Bool { arg() == 1.1 }
func callbackReturnString(arg: CallbackReturnString) -> Bool { arg() == "test string" }
func callbackReturnStringEmpty(arg: CallbackReturnStringEmpty) -> Bool { arg().isEmpty }
func callbackReturnStringN(arg: CallbackReturnStringN) -> Bool { arg() == nil }
func callbackReturnCallback(arg: CallbackReturnCallback) -> VoidCallback { arg() }
func callbackReturnCallbackN(arg: CallbackReturnCallbackN) -> Bool { arg() == nil }
func callbackReturnEnum(arg: CallbackReturnEnum) -> Bool { arg() == .case2 }
func callbackReturnDictionary(arg: CallbackReturnDictionary) -> Bool { passDictionary(arg: arg()) }
func callbackReturnDictionaryN(arg: CallbackReturnDictionaryN) -> Bool { arg() == nil }
func callbackReturnInterface(arg: CallbackReturnInterface) -> Bool { arg().test() }
func callbackReturnInterfaceN(arg: CallbackReturnInterfaceN) -> Bool { arg() == nil }

// MARK: - Array arguments

func callbackArgCharArray(arg: CallbackPassCharArray) -> Bool { arg([97, 98]) }
func callbackArgCharArrayEmpty(arg: CallbackPassCharArrayEmpty) -> Bool { arg([]) }
func callbackArgCharArrayN(arg: CallbackPassCharArrayN) -> Bool { arg(nil) }
func callbackArgBooleanArray(arg: CallbackPassBooleanArray) -> Bool { arg([true, false]) }
func callbackArgByteArray(arg: CallbackPassByteArray) -> Bool { arg([1, 2]) }
func callbackArgUbyteArray(arg: CallbackPassUByteArray) -> Bool { arg([1, 255]) }
func callbackArgShortArray(arg: CallbackPassShortArray) -> Bool { arg([1, 2]) }
func callbackArgUshortArray(arg: CallbackPassUShortArray) -> Bool { arg([1, 65535]) }
func callbackArgIntArray(arg: CallbackPassIntArray) -> Bool { arg([1, 2]) }
func callbackArgUintArray(arg: CallbackPassUIntArray) -> Bool { arg([1, 4294967295]) }
func callbackArgLongArray(arg: CallbackPassLongArray) -> Bool { arg([1, 2]) }
func callbackArgUlongArray(arg: CallbackPassULongArray) -> Bool { arg([1, 18446744073709551615]) }
func callbackArgFloatArray(arg: CallbackPassFloatArray) -> Bool { arg([1.1, 2.2]) }
func callbackArgDoubleArray(arg: CallbackPassDoubleArray) -> Bool { arg([1.1, 2.2]) }
func callbackArgStringArray(arg: CallbackPassStringArray) -> Bool { arg(["string1", "string2"]) }
func callbackArgStringArrayN(arg: CallbackPassStringArrayN) -> Bool { arg([nil, nil]) }
func callbackArgEnumArray(arg: CallbackPassEnumArray) -> Bool { arg([.case1, .case2]) }
func callbackArgDictionaryArray(arg: CallbackPassDictionaryArray) -> Bool { arg(returnDictionaryArray()) }
func callbackArgDictionaryArrayN(arg: CallbackPassDictionaryArrayN) -> Bool { arg([nil, nil]) }
func callbackArgInterfaceArray(arg: CallbackPassInterfaceArray) -> Bool { arg([MyInterface(), MyInterface()]) }
func callbackArgInterfaceArrayN(arg: CallbackPassInterfaceArrayN) -> Bool { arg([nil, nil]) }

// MARK: - Array return values

func callbackReturnCharArray(arg: CallbackReturnCharArray) -> Bool { passCharArray(arg: arg()) }
func callbackReturnCharArrayEmpty(arg: CallbackReturnCharArrayEmpty) -> Bool { arg().isEmpty }
func callbackReturnCharArrayN(arg: CallbackReturnCharArrayN) -> Bool { arg() == nil }
func callbackReturnBooleanArray(arg: CallbackReturnBooleanArray) -> Bool { passBooleanArray(arg: arg()) }
func callbackReturnByteArray(arg: CallbackReturnByteArray) -> Bool { passByteArray(arg: arg()) }
func callbackReturnUbyteArray(arg: CallbackReturnUByteArray) -> Bool { passUbyteArray(arg: arg()) }
func callbackReturnShortArray(arg: CallbackReturnShortArray) -> Bool { passShortArray(arg: arg()) }
func callbackReturnUshortArray(arg: CallbackReturnUShortArray) -> Bool { passUshortArray(arg: arg()) }
func callbackReturnIntArray(arg: CallbackReturnIntArray) -> Bool { passIntArray(arg: arg()) }
func callbackReturnUintArray(arg: CallbackReturnUIntArray) -> Bool { passUintArray(arg: arg()) }
func callbackReturnLongArray(arg: CallbackReturnLongArray) -> Bool { passLongArray(arg: arg()) }
func callbackReturnUlongArray(arg: CallbackReturnULongArray) -> Bool { passUlongArray(arg: arg()) }
func callbackReturnFloatArray(arg: CallbackReturnFloatArray) -> Bool { passFloatArray(arg: arg()) }
func callbackReturnDoubleArray(arg: CallbackReturnDoubleArray) -> Bool { passDoubleArray(arg: arg()) }
func callbackReturnStringArray(arg: CallbackReturnStringArray) -> Bool { passStringArray(arg: arg()) }
func callbackReturnStringArrayN(arg: CallbackReturnStringArrayN) -> Bool { passStringArrayN(arg: arg()) }
func callbackReturnEnumArray(arg: CallbackReturnEnumArray) -> Bool { passEnumArray(arg: arg()) }
func callbackReturnDictionaryArray(arg: CallbackReturnDictionaryArray) -> Bool { passDictionaryArray(arg: arg()) }
func callbackReturnDictionaryArrayN(arg: CallbackReturnDictionaryArrayN) -> Bool { passDictionaryArrayN(arg: arg()) }
func callbackReturnInterfaceArray(arg: CallbackReturnInterfaceArray) -> Bool { passInterfaceArray(arg: arg()) }
func callbackReturnInterfaceArrayN(arg: CallbackReturnInterfaceArrayN) -> Bool { passInterfaceArrayN(arg: arg()) }
