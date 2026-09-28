package com.huskerdev.nativekt.intellij.ndl.parser

import com.huskerdev.nativekt.NdlEnv
import com.huskerdev.webidl.*
import com.huskerdev.webidl.parser.*
import com.intellij.lang.*
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType


class NdlParser: PsiParser {

    override fun parse(rootType: IElementType, builder: PsiBuilder): ASTNode {
        builder.emit(
            definition = WebIDL.parseDefinitions(
                text = builder.originalText.toString(),
                types = NdlEnv.builtinTypes.keys
            ).first,
            rootType = rootType
        )
        return builder.treeBuilt
    }

    private fun PsiBuilder.emit(definition: IdlDefinition, rootType: IElementType? = null) {
        moveTo(definition.bounds.startOffset)

        val marker = mark()

        // Children
        definition.children.forEach { emit(it) }

        // Move to the definition end
        if(rootType != null) {
            while (!eof())
                advanceLexer()
        } else moveTo(definition.bounds.endOffset)

        marker.done(NdlDefinitionType.from(definition, rootType))
    }

    private fun PsiBuilder.moveTo(offset: Int) {
        if(offset == 0 && rawLookup(0) == TokenType.WHITE_SPACE)
            return
        while (currentOffset < offset && !eof() && currentOffset < originalText.length)
            advanceLexer()
    }
}