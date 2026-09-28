package com.huskerdev.nativekt.intellij.ndl.psi

import com.huskerdev.nativekt.intellij.ndl.lexer.NdlTokenType
import com.huskerdev.nativekt.intellij.ndl.parser.NdlDefinitionType
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange


class NdlFoldingBuilder: FoldingBuilder {

    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        if (node.elementType != NdlDefinitionType.FILE)
            return FoldingDescriptor.EMPTY_ARRAY

        return buildList {
            collect(node, document, this)
        }.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String = when (node.elementType) {
        NdlDefinitionType.INTERFACE, NdlDefinitionType.NAMESPACE,
        NdlDefinitionType.DICTIONARY, NdlDefinitionType.ENUM ->
            "{...}"
        else -> "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean =
        false

    private fun collect(node: ASTNode, document: Document, result: MutableList<FoldingDescriptor>) {
        when (node.elementType) {
            NdlDefinitionType.INTERFACE, NdlDefinitionType.NAMESPACE,
            NdlDefinitionType.DICTIONARY, NdlDefinitionType.ENUM,
            NdlDefinitionType.GETTER, NdlDefinitionType.SETTER ->
                braceRegion(node, document)
            NdlTokenType.BLOCK_COMMENT ->
                commentRegion(node)
            else -> null
        }?.let(result::add)

        node.getChildren(null).forEach { collect(it, document, result) }
    }

    private fun braceRegion(node: ASTNode, document: Document): FoldingDescriptor? {
        val open = node.findChildByType(NdlTokenType.L_CURLY_BRACKET)
        val close = node.findChildByType(NdlTokenType.R_CURLY_BRACKET)
        if (open == null || close == null) return null

        val start = open.textRange.startOffset
        val end = close.textRange.endOffset
        if (!isMultiline(document, start, end)) return null

        return FoldingDescriptor(node, TextRange(start, end), null, "{...}")
    }

    private fun commentRegion(node: ASTNode): FoldingDescriptor? {
        if (node.text.startsWith("/*") && node.text.contains('\n'))
            FoldingDescriptor(node, node.textRange, null, "/*...*/")
        return null
    }

    private fun isMultiline(document: Document, start: Int, end: Int): Boolean =
        start < end && document.getLineNumber(start) != document.getLineNumber(end - 1)
}