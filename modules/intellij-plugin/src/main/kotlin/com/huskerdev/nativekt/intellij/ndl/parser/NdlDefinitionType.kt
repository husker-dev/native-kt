package com.huskerdev.nativekt.intellij.ndl.parser

import com.huskerdev.nativekt.intellij.ndl.NdlLanguage
import com.huskerdev.webidl.parser.*
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType

class NdlDefinitionType(
    debugName: String,
): IElementType(debugName, NdlLanguage.INSTANCE) {
    companion object {
        @JvmField val FILE = IFileElementType("NDL_FILE", NdlLanguage.INSTANCE)

        @JvmField val TYPE = NdlDefinitionType("TYPE")
        @JvmField val UNION_TYPE = NdlDefinitionType("UNION_TYPE")

        @JvmField val NAME = NdlDefinitionType("NAME")
        @JvmField val BUILTIN_NAME = NdlDefinitionType("BUILTIN_NAME")
        @JvmField val REFERENCE_NAME = NdlDefinitionType("REFERENCE_NAME")

        @JvmField val ATTRIBUTE_BLOCK = NdlDefinitionType("ATTRIBUTE_BLOCK")
        @JvmField val ATTRIBUTE = NdlDefinitionType("ATTRIBUTE")

        @JvmField val INTERFACE = NdlDefinitionType("INTERFACE")
        @JvmField val NAMESPACE = NdlDefinitionType("NAMESPACE")
        @JvmField val DICTIONARY = NdlDefinitionType("DICTIONARY")
        @JvmField val CALLBACK = NdlDefinitionType("CALLBACK")
        @JvmField val TYPEDEF = NdlDefinitionType("TYPEDEF")
        @JvmField val ENUM = NdlDefinitionType("ENUM")

        @JvmField val ENUM_ELEMENT = NdlDefinitionType("ENUM_VALUE")
        @JvmField val INCLUDES = NdlDefinitionType("INCLUDES")
        @JvmField val IMPLEMENTS = NdlDefinitionType("IMPLEMENTS")
        @JvmField val CONSTRUCTOR = NdlDefinitionType("CONSTRUCTOR")
        @JvmField val OPERATION = NdlDefinitionType("METHOD")
        @JvmField val FIELD = NdlDefinitionType("FIELD")
        @JvmField val ARGUMENT = NdlDefinitionType("ARGUMENT")
        @JvmField val ITERABLE = NdlDefinitionType("ITERABLE")
        @JvmField val ASYNC_ITERABLE_LIKE = NdlDefinitionType("ASYNC_ITERABLE_LIKE")
        @JvmField val MAP_LIKE = NdlDefinitionType("MAP_LIKE")
        @JvmField val SET_LIKE = NdlDefinitionType("SET_LIKE")
        @JvmField val STRINGIFIER = NdlDefinitionType("STRINGIFIER")
        @JvmField val GETTER = NdlDefinitionType("GETTER")
        @JvmField val SETTER = NdlDefinitionType("SETTER")

        fun from(definition: IdlDefinition, rootType: IElementType? = null) = when(definition) {
            is IdlRoot              -> rootType!!
            is IdlName              -> when {
                definition.isBuiltin   -> BUILTIN_NAME
                definition.isReference -> REFERENCE_NAME
                else                   -> NAME
            }
            is IdlInterface         -> INTERFACE
            is IdlNamespace         -> NAMESPACE
            is IdlDictionary        -> DICTIONARY
            is IdlEnum              -> ENUM
            is IdlEnumElement       -> ENUM_ELEMENT
            is IdlCallbackFunction  -> CALLBACK
            is IdlTypeDef           -> TYPEDEF
            is IdlIncludes          -> INCLUDES
            is IdlImplements        -> IMPLEMENTS
            is IdlConstructor       -> CONSTRUCTOR
            is IdlOperation         -> OPERATION
            is IdlField ->          if (definition.isOperationArgument) ARGUMENT else FIELD
            is IdlGetter            -> GETTER
            is IdlSetter            -> SETTER
            is IdlStringifier       -> STRINGIFIER
            is IdlAttributes        -> ATTRIBUTE_BLOCK
            is IdlExtendedAttribute -> ATTRIBUTE
            is IdlAsyncIterableLike -> ASYNC_ITERABLE_LIKE
            is IdlIterable          -> ITERABLE
            is IdlMapLike           -> MAP_LIKE
            is IdlSetLike           -> SET_LIKE
            is IdlType.Default      -> TYPE
            is IdlType.Union        -> UNION_TYPE
        }
    }
}