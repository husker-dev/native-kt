package com.huskerdev.nativekt.jvm

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Marker(val id: Int)