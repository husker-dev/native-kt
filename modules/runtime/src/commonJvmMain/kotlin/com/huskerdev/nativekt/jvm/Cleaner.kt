package com.huskerdev.nativekt.jvm

import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.lang.reflect.Method
import kotlin.concurrent.thread

fun <T : Any> createCleaner(obj: Any, resource: T, callback: (T) -> Unit): Any =
    cleaner.create(obj, resource, callback)

private val cleaner: Cleaner by lazy {
    // 1. java.lang.ref.Cleaner (Java 9+ / Android 33+)
    try {
        return@lazy J9Cleaner()
    } catch (_: Throwable) { /* not available */ }

    // 2. sun.misc.Cleaner (Java 4–8 / old Android)
    try {
        return@lazy SunMiscCleaner()
    } catch (_: Throwable) { /* not available */ }

    // 3. PhantomReference + ReferenceQueue
    return@lazy CustomCleaner()
}

private interface Cleaner {
    fun <T : Any> create(obj: Any, resource: T, callback: (T) -> Unit): Any
}

private class J9Cleaner: Cleaner {
    private val cleanerInstance: Any
    private val registerFunc: Method

    init {
        val clazz = Class.forName("java.lang.ref.Cleaner")
        cleanerInstance = clazz.getMethod("create").invoke(null)
        registerFunc = clazz.getMethod("register", Any::class.java, Runnable::class.java)
    }

    override fun <T : Any> create(obj: Any, resource: T, callback: (T) -> Unit): Any =
        registerFunc.invoke(cleanerInstance, obj, Runnable { callback(resource) })
}

private class SunMiscCleaner: Cleaner {
    private val createFunc: Method

    init {
        val clazz = Class.forName("sun.misc.Cleaner")
        createFunc = clazz.getMethod("create", Any::class.java, Runnable::class.java)
    }

    override fun <T : Any> create(obj: Any, resource: T, callback: (T) -> Unit): Any =
        createFunc.invoke(null, obj, Runnable { callback(resource) })
}

private class CustomCleaner: Cleaner {
    private val queue = ReferenceQueue<Any>()
    private val tracked = mutableListOf<CleanerRefEntry<*>>()
    private val lock = Any()

    init {
        thread(name = "nativekt-cleaner-thread", isDaemon = true) {
            while (true) {
                val ref = queue.remove() as? CleanerRefEntry<*> ?: continue
                try {
                    ref.callback(ref.resource)
                } catch (_: Throwable) {}
                synchronized(lock) { tracked.remove(ref) }
            }
        }
    }

    override fun <T : Any> create(obj: Any, resource: T, callback: (T) -> Unit): Any {
        val entry = CleanerRefEntry(obj, queue, resource, callback)
        synchronized(lock) { tracked.add(entry) }
        return entry
    }

    class CleanerRefEntry<T>(
        target: Any,
        queue: ReferenceQueue<Any>,
        val resource: T,
        val callback: (T) -> Unit
    ) : PhantomReference<Any>(target, queue)
}
