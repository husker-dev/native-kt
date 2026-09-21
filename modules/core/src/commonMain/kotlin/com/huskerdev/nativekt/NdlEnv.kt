package com.huskerdev.nativekt

import com.huskerdev.webidl.WebIDLEnv
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind

class NdlEnv: WebIDLEnv {
    override val builtinTypes = hashMapOf(
        "void" to WebIDLBuiltinKind.VOID,
        "char" to WebIDLBuiltinKind.CHAR,
        "boolean" to WebIDLBuiltinKind.BOOLEAN,
        "byte" to WebIDLBuiltinKind.BYTE,
        "ubyte" to WebIDLBuiltinKind.UNSIGNED_BYTE,
        "short" to WebIDLBuiltinKind.SHORT,
        "ushort" to WebIDLBuiltinKind.UNSIGNED_SHORT,
        "int" to WebIDLBuiltinKind.INT,
        "uint" to WebIDLBuiltinKind.UNSIGNED_INT,
        "long" to WebIDLBuiltinKind.LONG,
        "ulong" to WebIDLBuiltinKind.UNSIGNED_LONG,
        "float" to WebIDLBuiltinKind.FLOAT,
        "double" to WebIDLBuiltinKind.DOUBLE,
        "string" to WebIDLBuiltinKind.STRING,
        "Array" to WebIDLBuiltinKind.LIST,
    )
}