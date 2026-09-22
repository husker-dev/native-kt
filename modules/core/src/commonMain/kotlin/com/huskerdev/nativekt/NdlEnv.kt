package com.huskerdev.nativekt

import com.huskerdev.webidl.WebIDLEnv
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind

class NdlEnv: WebIDLEnv {
    override val builtinTypes = hashMapOf(
        "void" to WebIDLBuiltinKind.VOID,
        "char" to WebIDLBuiltinKind.CHAR,
        "bool" to WebIDLBuiltinKind.BOOLEAN,
        "i8" to WebIDLBuiltinKind.BYTE,
        "u8" to WebIDLBuiltinKind.UNSIGNED_BYTE,
        "i16" to WebIDLBuiltinKind.SHORT,
        "u16" to WebIDLBuiltinKind.UNSIGNED_SHORT,
        "i32" to WebIDLBuiltinKind.INT,
        "u32" to WebIDLBuiltinKind.UNSIGNED_INT,
        "i64" to WebIDLBuiltinKind.LONG,
        "u64" to WebIDLBuiltinKind.UNSIGNED_LONG,
        "f32" to WebIDLBuiltinKind.FLOAT,
        "f64" to WebIDLBuiltinKind.DOUBLE,
        "string" to WebIDLBuiltinKind.STRING,
        "Array" to WebIDLBuiltinKind.LIST,
    )
}