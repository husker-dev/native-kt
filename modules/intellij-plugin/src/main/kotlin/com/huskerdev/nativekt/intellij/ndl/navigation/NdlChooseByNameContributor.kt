package com.huskerdev.nativekt.intellij.ndl.navigation

import com.huskerdev.nativekt.intellij.ndl.NdlUtils.collectAllTypeDeclarations
import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter


class NdlChooseByNameContributor: ChooseByNameContributorEx {
    override fun processNames(
        processor: Processor<in String>,
        scope: GlobalSearchScope,
        filter: IdFilter?
    ) {
        val names = collectAllTypeDeclarations(scope.project!!).map { it.text }
        ContainerUtil.process(names, processor)
    }

    override fun processElementsWithName(
        name: String,
        processor: Processor<in NavigationItem>,
        parameters: FindSymbolParameters
    ) {
        ContainerUtil.process(collectAllTypeDeclarations(parameters.project, name), processor)
    }
}