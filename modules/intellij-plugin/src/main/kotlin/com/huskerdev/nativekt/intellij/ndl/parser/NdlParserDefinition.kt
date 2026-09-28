package com.huskerdev.nativekt.intellij.ndl.parser

import com.huskerdev.nativekt.intellij.ndl.lexer.NdlTokenType
import com.huskerdev.nativekt.intellij.ndl.lexer.NdlLexer
import com.huskerdev.nativekt.intellij.ndl.psi.*
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet


class NdlParserDefinition: ParserDefinition {

    override fun createLexer(project: Project) =
        NdlLexer()

    override fun createParser(project: Project) =
        NdlParser()

    override fun getFileNodeType(): IFileElementType =
        NdlDefinitionType.FILE

    override fun getCommentTokens(): TokenSet =
        TokenSet.create(NdlTokenType.LINE_COMMENT, NdlTokenType.BLOCK_COMMENT)

    override fun getStringLiteralElements(): TokenSet =
        TokenSet.create(NdlTokenType.STRING)

    override fun createElement(node: ASTNode): PsiElement = when (node.elementType) {
        NdlDefinitionType.TYPE -> NdlPsiType(node)
        NdlDefinitionType.UNION_TYPE -> NdlPsiUnionType(node)

        NdlDefinitionType.NAME -> NdlPsiName(node)
        NdlDefinitionType.REFERENCE_NAME -> NdlPsiTypeReference(node)
        NdlDefinitionType.BUILTIN_NAME -> NdlPsiBuiltinTypeReference(node)

        NdlDefinitionType.ATTRIBUTE_BLOCK -> NdlPsiAttributeBlock(node)
        NdlDefinitionType.ATTRIBUTE -> NdlPsiAttribute(node)

        NdlDefinitionType.INTERFACE -> NdlPsiInterface(node)
        NdlDefinitionType.NAMESPACE -> NdlPsiNamespace(node)
        NdlDefinitionType.DICTIONARY -> NdlPsiDictionary(node)
        NdlDefinitionType.CALLBACK -> NdlPsiCallback(node)
        NdlDefinitionType.TYPEDEF -> NdlPsiTypedef(node)
        NdlDefinitionType.ENUM -> NdlPsiEnum(node)

        NdlDefinitionType.ENUM_ELEMENT -> NdlPsiEnumElement(node)
        NdlDefinitionType.INCLUDES -> NdlPsiIncludes(node)
        NdlDefinitionType.IMPLEMENTS -> NdlPsiImplements(node)
        NdlDefinitionType.CONSTRUCTOR -> NdlPsiConstructor(node)
        NdlDefinitionType.OPERATION -> NdlPsiOperation(node)
        NdlDefinitionType.FIELD -> NdlPsiField(node)
        NdlDefinitionType.ARGUMENT -> NdlPsiArgument(node)
        NdlDefinitionType.ITERABLE -> NdlPsiIterable(node)
        NdlDefinitionType.ASYNC_ITERABLE_LIKE -> NdlPsiAsyncIterable(node)
        NdlDefinitionType.MAP_LIKE -> NdlPsiMapLike(node)
        NdlDefinitionType.SET_LIKE -> NdlPsiSetLike(node)
        NdlDefinitionType.STRINGIFIER -> NdlPsiStringifier(node)
        NdlDefinitionType.GETTER -> NdlPsiGetter(node)
        NdlDefinitionType.SETTER -> NdlPsiSetter(node)

        else -> ASTWrapperPsiElement(node)
    }

    override fun createFile(viewProvider: FileViewProvider) =
        NdlPsiFile(viewProvider)
}