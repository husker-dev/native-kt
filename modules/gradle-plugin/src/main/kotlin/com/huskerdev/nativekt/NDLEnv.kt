package com.huskerdev.nativekt

import com.huskerdev.webidl.WebIDLEnv
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind

class NDLEnv: WebIDLEnv {
    override val builtinTypes = hashMapOf(
        "void" to WebIDLBuiltinKind.VOID,
        "char" to WebIDLBuiltinKind.CHAR,
        "boolean" to WebIDLBuiltinKind.BOOLEAN,
        "byte" to WebIDLBuiltinKind.BYTE,
        "short" to WebIDLBuiltinKind.SHORT,
        "int" to WebIDLBuiltinKind.INT,
        "long" to WebIDLBuiltinKind.LONG,
        "float" to WebIDLBuiltinKind.FLOAT,
        "double" to WebIDLBuiltinKind.DOUBLE,
        "string" to WebIDLBuiltinKind.STRING,
        "Array" to WebIDLBuiltinKind.LIST
    )
}