package com.huskerdev.nativekt.intellij.ndl.psi

import com.huskerdev.nativekt.intellij.ndl.NdlUtils
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.HintedReferenceHost
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.lastLeaf
import com.intellij.psi.util.startOffset
import javax.swing.Icon

sealed class NdlPsi(node: ASTNode): ASTWrapperPsiElement(node)

sealed class NdlPsiNamedDeclaration(node: ASTNode): NdlPsi(node), PsiNameIdentifierOwner {

    override fun getName(): String = nameIdentifier.text

    override fun setName(newName: String): PsiElement {
        NdlUtils.rename(nameIdentifier, newName)
        return this
    }

    override fun getNameIdentifier(): PsiElement =
        findChildByClass(NdlPsiName::class.java)!!.lastLeaf()

    override fun getTextOffset(): Int = nameIdentifier.startOffset

    override fun getUseScope(): SearchScope =
        LocalSearchScope(containingFile)

    override fun getPresentation(): ItemPresentation = object : ItemPresentation {
        override fun getPresentableText(): String = nameIdentifier.text
        override fun getLocationString(): String = containingFile?.name ?: ""
        override fun getIcon(unused: Boolean): Icon? = this@NdlPsiNamedDeclaration.getIcon(0)
    }
}

class NdlPsiName(node: ASTNode): NdlPsi(node)
class NdlPsiBuiltinTypeReference(node: ASTNode): NdlPsi(node)
class NdlPsiTypeReference(node: ASTNode): NdlPsi(node), HintedReferenceHost {
    override fun getReferences(hints: PsiReferenceService.Hints): Array<PsiReference> =
        arrayOf(NdlReference(this))

    override fun shouldAskParentForReferences(p0: PsiReferenceService.Hints): Boolean = false
}


sealed class NdlPsiTypeDeclaration(node: ASTNode): NdlPsiNamedDeclaration(node)

class NdlPsiInterface(node: ASTNode): NdlPsiTypeDeclaration(node)
class NdlPsiDictionary(node: ASTNode): NdlPsiTypeDeclaration(node)
class NdlPsiCallback(node: ASTNode): NdlPsiTypeDeclaration(node)
class NdlPsiTypedef(node: ASTNode): NdlPsiTypeDeclaration(node)
class NdlPsiEnum(node: ASTNode): NdlPsiTypeDeclaration(node)

class NdlPsiNamespace(node: ASTNode): NdlPsiNamedDeclaration(node)
class NdlPsiEnumElement(node: ASTNode): NdlPsiNamedDeclaration(node)
class NdlPsiOperation(node: ASTNode): NdlPsiNamedDeclaration(node)
class NdlPsiField(node: ASTNode): NdlPsiNamedDeclaration(node)
class NdlPsiAttribute(node: ASTNode): NdlPsiNamedDeclaration(node)

class NdlPsiAttributeBlock(node: ASTNode): NdlPsi(node)

class NdlPsiType(node: ASTNode): NdlPsi(node)
class NdlPsiUnionType(node: ASTNode): NdlPsi(node)
class NdlPsiIncludes(node: ASTNode): NdlPsi(node)
class NdlPsiImplements(node: ASTNode): NdlPsi(node)
class NdlPsiConstructor(node: ASTNode): NdlPsi(node)
class NdlPsiArgument(node: ASTNode): NdlPsi(node)
class NdlPsiIterable(node: ASTNode): NdlPsi(node)
class NdlPsiAsyncIterable(node: ASTNode): NdlPsi(node)
class NdlPsiMapLike(node: ASTNode): NdlPsi(node)
class NdlPsiSetLike(node: ASTNode): NdlPsi(node)
class NdlPsiStringifier(node: ASTNode): NdlPsi(node)
class NdlPsiGetter(node: ASTNode): NdlPsi(node)
class NdlPsiSetter(node: ASTNode): NdlPsi(node)