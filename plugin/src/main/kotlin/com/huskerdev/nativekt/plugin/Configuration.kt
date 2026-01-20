package com.huskerdev.nativekt.plugin

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class Configuration @Inject constructor(
    objects: ObjectFactory
): NamedDomainObjectContainer<NativeModule> by objects.domainObjectContainer(NativeModule::class.java)

open class NativeModule @Inject constructor(
    val name: String,
    objects: ObjectFactory
) {
    //var projectName: String = name
    var classPath: String = "natives.$name"
}