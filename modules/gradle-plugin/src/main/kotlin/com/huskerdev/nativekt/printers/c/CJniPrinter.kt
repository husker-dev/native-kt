package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.plugin.NativeKtAndroidInterface
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File

class CJniPrinter(
    val context: NativeModuleContext,
    target: File,
    headerTarget: File,
    val isAndroid: Boolean
) {
    private val isAndroidCriticalEnabled =
        isAndroid && (context.extension as? NativeKtAndroidInterface)?.useAndroidCriticalNative ?: false

    init {
        val castsFunctions = arrayListOf<String>()
        target.writeText(buildString {
            append("#include \"${headerTarget.name}\"\n\n")
            printBasicCasts(castsFunctions)
            printDictionaries(castsFunctions)
            printInterfaces(castsFunctions)
            printCallbacks(castsFunctions)
            printRegister()
            printFunctions()
            printFunctionsList()
        })
        headerTarget.writeText(buildString {
            printHeader()
            castsFunctions.joinTo(this, prefix = "\n", separator = "\n")
        })
    }

    private fun StringBuilder.printHeader() {
        appendLine("""
            #pragma once
            
            #include <jni.h>
            #include "api.h"
            
            static JavaVM *jvm;
        """.trimIndent())

        append("\nstatic void** JNI_functions(bool useCritical);\n")

        val classes = arrayListOf("_class")
        val methods = arrayListOf<String>()
        val objects = arrayListOf<String>()

        // String
        if (context.hasString) {
            classes += "class_string"
            objects += "string_utf8_const"
        }
        if (context.hasStringToKotlinCast)
            methods += "string_constructor"
        if (context.hasStringToNativeCast) methods += "string_get_bytes"

        // Enums
        if (context.hasEnums) {
            if (context.hasEnumToNativeCast)
                methods.add("enum_ordinal")
            context.usedEnums.forEach { enum ->
                if (enum in context.toKotlinDeclarations) {
                    classes += enum.className
                    methods += enum.castMethod
                }
            }
        }

        // Dictionaries
        if(context.hasDictionaries) {
            context.usedDictionaries.forEach { dictionary ->
                if (dictionary in context.toKotlinDeclarations) {
                    classes += dictionary.className
                    methods += dictionary.constructorMethod
                }
                if (dictionary in context.toNativeDeclarations) {
                    context.allFields[dictionary]!!.forEach { field ->
                        methods += field.dictionaryFieldMethod(dictionary)
                    }
                }
            }
        }

        // Interfaces
        if(context.hasInterfaces) {
            if (context.hasInterfaceToNativeCast)
                methods += "interface_ptr"
            context.usedInterfaces.forEach { inter ->
                if (inter in context.toKotlinDeclarations)
                    classes += inter.className
                if (inter in context.toNativeDeclarations)
                    methods += inter.constructorMethod
            }
        }

        // Callbacks
        if (context.hasCallbacks) {
            methods += "callback_equals"
            methods += "callback_hash_code"
            context.usedCallbacks.forEach { callback ->
                methods += callback.invokeMethod
            }
        }

        // Print
        mapOf(
            classes to "jclass",
            methods to "jmethodID",
            objects to "jobject"
        ).forEach { (list, type) ->
            if (list.isNotEmpty()) {
                list.chunked(4).joinTo(
                    buffer = this,
                    separator = ",\n\t",
                    prefix = "\nstatic $type ",
                    postfix = ";\n"
                ) { it.joinToString() }
            }
        }
    }

    private fun StringBuilder.printRegister() {
        val nextFunc = "JNI_next_function(env, names, signatures, &methods_count)"
        val nextClass = "JNI_next_class(env, classes, &classes_count)"

        appendLine("""
            
            static char** JNI_split(JNIEnv* env, jstring of, jint* count_ref) {
                const char* str = (*env)->GetStringUTFChars(env, of, JNI_FALSE);
                
                jint count = 1;
                for(jint i = 0; str[i]; i++)
                    if(str[i] == ',') count++;
                if(count_ref != NULL)
                    *count_ref = count;
                
                char** arr = malloc(count * sizeof(void*));
                const char* cur_addr = str;
                
                for(jint i = 0; i < count; i++) {
                    const char* next = strchr(cur_addr, ',');
                    jint length = next == NULL ? strlen(cur_addr) : (next - cur_addr);
                    
                    arr[i] = malloc(length + 1);
                    arr[i][length] = '\0';
                    strncpy(arr[i], cur_addr, length);
                    
                    if (next != NULL)
                        cur_addr = next + 1;
                }
                (*env)->ReleaseStringUTFChars(env, of, str);
                return arr;
            }
            
            static jmethodID JNI_next_function(JNIEnv* env, char** names, char** signatures, jint* index) {
                const jmethodID result = (*env)->GetStaticMethodID(env, _class, names[*index], signatures[*index]);
                *index = *index + 1;
                return result;
            }
            
            static jclass JNI_next_class(JNIEnv* env, char** classes, jint* index) {
                fflush(stdout);
            	const jclass result = (*env)->NewGlobalRef(env, (*env)->FindClass(env, classes[*index]));
            	*index = *index + 1;
            	return result;
            }
            
            JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
                JNIEnv* env = NULL;
                if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6) != JNI_OK)
                    return JNI_ERR;
                jvm = vm;
                    
                // Get Properties
                jclass system_class = (*env)->FindClass(env, "java/lang/System");
                jmethodID get_prop_method = (*env)->GetStaticMethodID(env, system_class, "getProperty", "(Ljava/lang/String;)Ljava/lang/String;");

                // Get class
                jstring class_name_string = (jstring) (*env)->CallStaticObjectMethod(env, system_class, get_prop_method, (*env)->NewStringUTF(env, "nativekt.${context.moduleName}.class"));
                if(class_name_string == NULL)
                    return JNI_VERSION_1_4;
                const char* class_name = (*env)->GetStringUTFChars(env, class_name_string, JNI_FALSE);
                _class = (*env)->NewGlobalRef(env, (*env)->FindClass(env, class_name));
                
                (*env)->ReleaseStringUTFChars(env, class_name_string, class_name);
                (*env)->DeleteLocalRef(env, class_name_string);
                
                // Get names
                jstring names_string = (jstring) (*env)->CallStaticObjectMethod(env, system_class, get_prop_method, (*env)->NewStringUTF(env, "nativekt.${context.moduleName}.names"));
                jint names_count = 0;
                char** names = JNI_split(env, names_string, &names_count);
                (*env)->DeleteLocalRef(env, names_string);
                
                // Get signatures
                jstring signatures_string = (jstring) (*env)->CallStaticObjectMethod(env, system_class, get_prop_method, (*env)->NewStringUTF(env, "nativekt.${context.moduleName}.signatures"));
                jint signatures_count = 0;
                char** signatures = JNI_split(env, signatures_string, &signatures_count);
                (*env)->DeleteLocalRef(env, signatures_string);
                
                // Get classes
                jstring classes_string = (jstring) (*env)->CallStaticObjectMethod(env, system_class, get_prop_method, (*env)->NewStringUTF(env, "nativekt.${context.moduleName}.classes"));
                jint classes_count = 0;
                char** classes = JNI_split(env, classes_string, NULL);
                (*env)->DeleteLocalRef(env, classes_string);
                
                // Get critical
                jstring critical_string = (jstring) (*env)->CallStaticObjectMethod(env, system_class, get_prop_method, (*env)->NewStringUTF(env, "nativekt.${context.moduleName}.critical"));
                bool useCritical = false;
                if(critical_string != NULL) {
                    jchar buf;
                    (*env)->GetStringRegion(env, critical_string, 0, 1, &buf);
                    useCritical = buf == '1';
                }
                (*env)->DeleteLocalRef(env, critical_string);
                
                // Register native methods
                jint methods_count = ${context.allOperations.size};
                void** functions = JNI_functions(useCritical);
                JNINativeMethod* jni_methods = (JNINativeMethod*) (methods_count > 0 ? malloc(methods_count * sizeof(JNINativeMethod)) : NULL);
                for(jint i = 0; i < methods_count; i++)
                    jni_methods[i] = (JNINativeMethod) { names[i], signatures[i], functions[i] };
                (*env)->RegisterNatives(env, _class, jni_methods, methods_count);
        """.trimIndent())

        // String
        if (context.hasString) {
            appendLine("""
                
                // String
                class_string = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "java/lang/String"));
                string_utf8_const = (*env)->NewGlobalRef(env, (*env)->NewStringUTF(env, "UTF-8"));
            """.replaceIndent("\t"))
            if (context.hasStringToKotlinCast) appendLine("""
                string_constructor = (*env)->GetMethodID(env, class_string, "<init>", "([BLjava/lang/String;)V");
            """.replaceIndent("\t"))
            if (context.hasStringToNativeCast) appendLine("""
                string_get_bytes = (*env)->GetMethodID(env, class_string, "getBytes", "(Ljava/lang/String;)[B");
            """.replaceIndent("\t"))
        }

        // Enums
        if (context.hasEnums) {
            append("\n\t// Enums")
            if (context.hasEnumToNativeCast)
                append("\n\tenum_ordinal = (*env)->GetMethodID(env, (*env)->FindClass(env, \"java/lang/Enum\"), \"ordinal\", \"()I\");")
            context.usedEnums.forEach { enum ->
                if (enum in context.toKotlinDeclarations) {
                    append("\n\t${enum.className} = $nextClass;")
                    append("\n\t${enum.castMethod} = $nextFunc;")
                }
            }
            append("\n")
        }

        // Dictionaries
        if (context.hasDictionaries) {
            append("\n\t// Dictionaries")
            context.usedDictionaries.forEach { dictionary ->
                if (dictionary in context.toKotlinDeclarations) {
                    append("\n\t${dictionary.className} = $nextClass;")
                    append("\n\t${dictionary.constructorMethod} = $nextFunc;")
                }
                if (dictionary in context.toNativeDeclarations) {
                    context.allFields[dictionary]!!.forEach { field ->
                        append("\n\t${field.dictionaryFieldMethod(dictionary)} = $nextFunc;")
                    }
                }
            }
            append("\n")
        }

        // Interfaces
        if (context.hasInterfaces) {
            append("\n\t// Interfaces")
            if (context.hasInterfaceToNativeCast)
                append("\n\tinterface_ptr = $nextFunc;")
            context.usedInterfaces.forEach { inter ->
                if (inter in context.toKotlinDeclarations) append("\n\t${inter.className} = $nextClass;")
                if (inter in context.toNativeDeclarations) append("\n\t${inter.constructorMethod} = $nextFunc;")
            }
            append("\n")
        }

        // Callbacks
        if (context.hasCallbacks) {
            append("\n\t// Callbacks")
            append("\n\tcallback_equals = $nextFunc;")
            append("\n\tcallback_hash_code = $nextFunc;")
            context.usedCallbacks.forEach { callback ->
                append("\n\t${callback.invokeMethod} = $nextFunc;")
            }
            append("\n")
        }

        appendLine("""
                
                for(jint i = 0; i < names_count; i++)
                    free((void*) names[i]);
                free((void*) names);
                for(jint i = 0; i < signatures_count; i++)
                    free((void*) signatures[i]);
                free((void*) signatures);
                for(jint i = 0; i < classes_count; i++)
                    free((void*) classes[i]);
                free((void*) classes);
                if(methods_count != 0) {
                    free((void*) functions);
                    free((void*) jni_methods);
                }
                return JNI_VERSION_1_4;
            }
        """.trimIndent())
    }

    private fun StringBuilder.printBasicCasts(castsFunctions: ArrayList<String>) {

        // String
        if (context.hasString) {
            printLabel("String")
            if (context.hasStringToNativeCast) {
                appendLine("""
                    
                    static void* JNI_to_native_string(JNIEnv *env, jstring obj) {
                        if(obj == NULL) return NULL;
    
                        jbyteArray bytes = (jbyteArray) (*env)->CallObjectMethod(env, obj, string_get_bytes, string_utf8_const);
                        jsize length = (*env)->GetStringLength(env, obj);
                        jsize size = (*env)->GetArrayLength(env, bytes);
                        jbyte* data = (jbyte*) ${context.mangle("alloc")}(size);
                        (*env)->GetByteArrayRegion(env, bytes, 0, size, data);
                        void* result = ${context.mangle("string_new")}((const char*) data, length, size, false);
                        (*env)->DeleteLocalRef(env, bytes);
                        return result;
                    }
                """.trimIndent())
                castsFunctions += "static void* JNI_to_native_string(JNIEnv *env, jstring obj);"
            }
            if (context.hasStringToKotlinCast) {
                appendLine("""
                    
                    static jstring JNI_to_kotlin_string(JNIEnv *env, void* str, bool free) {
                        if(str == NULL) return NULL;
    
                        jint size = (jint) ${context.mangle("string_size")}(str);
                        jbyteArray bytes = (*env)->NewByteArray(env, size);
                        (*env)->SetByteArrayRegion(env, bytes, 0, size, (jbyte*) ${context.mangle("string_data")}(str));
    
                        jstring result = (jstring) (*env)->NewObject(env, class_string, string_constructor, bytes, string_utf8_const);
                        (*env)->DeleteLocalRef(env, bytes);
                        if (free) ${context.mangle("string_free")}(str);
                        return result;
                    }
                """.trimIndent())
                castsFunctions += "static jstring JNI_to_kotlin_string(JNIEnv *env, void* str, bool free);"
            }
        }

        // Enum
        if (context.hasEnums) {
            append("\n// Enum\n")
            if (context.hasEnumToNativeCast) {
                appendLine("""
                    
                    static int32_t JNI_to_native_enum(JNIEnv* env, const jobject of) {
                        return (*env)->CallIntMethod(env, of, enum_ordinal);
                    }
                """.trimIndent())
                castsFunctions += "static int32_t JNI_to_native_enum(JNIEnv* env, const jobject of);"
            }
            if (context.hasEnumToKotlinCast) {
                appendLine("""
                    
                    static jobject JNI_to_kotlin_enum(JNIEnv* env, const int32_t of, jmethodID cast_method) {
                        return (*env)->CallStaticObjectMethod(env, _class, cast_method, of);
                    }
                """.trimIndent())
                castsFunctions += "static jobject JNI_to_kotlin_enum(JNIEnv* env, const int32_t of, jmethodID cast_method);"
            }
        }

        // Primitive arrays
        listOf(
            Triple("char", context.hasCharArrayToNativeCast, context.hasCharArrayToKotlinCast),
            Triple("boolean", context.hasBooleanArrayToNativeCast, context.hasBooleanArrayToKotlinCast),
            Triple("byte",
                context.hasByteArrayToNativeCast || context.hasUByteArrayToNativeCast,
                context.hasByteArrayToKotlinCast || context.hasUByteArrayToKotlinCast),
            Triple("short",
                context.hasShortArrayToNativeCast || context.hasUShortArrayToNativeCast,
                context.hasShortArrayToKotlinCast || context.hasUShortArrayToKotlinCast),
            Triple("int",
                context.hasIntArrayToNativeCast || context.hasUIntArrayToNativeCast,
                context.hasIntArrayToKotlinCast || context.hasUIntArrayToKotlinCast),
            Triple("long",
                context.hasLongArrayToNativeCast || context.hasULongArrayToNativeCast,
                context.hasLongArrayToKotlinCast || context.hasULongArrayToKotlinCast),
            Triple("float", context.hasFloatArrayToNativeCast, context.hasFloatArrayToKotlinCast),
            Triple("double", context.hasDoubleArrayToNativeCast, context.hasDoubleArrayToKotlinCast),
        ).forEach { (name, hasToNative, hasToKotlin) ->
            val capitalized = name.capitalized()

            val jCast = if (name == "boolean" || name == "long") "(j$name*) " else ""
            val kCast = when (name) {
                "long" -> "(const int64_t*) "
                "boolean" -> "(const bool*) "
                else -> ""
            }

            if (hasToNative || hasToKotlin)
                append("\n// Array: $capitalized\n")

            if (hasToNative) {
                appendLine("""
                
                    static void* JNI_to_native_${name}array(JNIEnv *env, const j${name}Array arr) {
                        if(arr == NULL) return NULL;
                        jint length = (*env)->GetArrayLength(env, arr);
                        j$name* elements = (j$name*) ${context.mangle("alloc")}(length * sizeof(j$name));
                        (*env)->Get${capitalized}ArrayRegion(env, arr, 0, length, elements);
                        return ${context.mangle("${name}array_new")}(${kCast}elements, length, false);
                    }
                """.trimIndent())
                castsFunctions += "static void* JNI_to_native_${name}array(JNIEnv *env, const j${name}Array arr);"
            }
            if (hasToKotlin) {
                appendLine("""
                    
                    static j${name}Array JNI_to_kotlin_${name}array(JNIEnv *env, void* arr, const bool free) {
                        if(arr == NULL) return NULL;
                        const jint length = ${context.mangle("${name}array_length")}(arr);
                        const j${name}Array result = (*env)->New${capitalized}Array(env, length);
                        (*env)->Set${capitalized}ArrayRegion(env, result, 0, length, $jCast${context.mangle("${name}array_elements")}(arr));
                        if (free) ${context.mangle("${name}array_free")}(arr);
                        return result;
                    }
                """.trimIndent())
                castsFunctions += "static j${name}Array JNI_to_kotlin_${name}array(JNIEnv *env, void* arr, const bool free);"
            }
        }

        // Enum arrays
        if (context.hasEnumArray) {
            append("\n// Array: Enum\n")

            if (context.hasEnumArrayToNativeCast) {
                appendLine("""
                    
                    static void* JNI_to_native_enum_array(JNIEnv *env, jobjectArray src) {
                        if(src == NULL) return NULL;
                        jsize length = (*env)->GetArrayLength(env, src);
                        int32_t* elements = (int32_t*) malloc(length * sizeof(int32_t));
                        for(int i = 0; i < length; i++) {
                            jobject element = (*env)->GetObjectArrayElement(env, src, i);
                            elements[i] = JNI_to_native_enum(env, element);
                            (*env)->DeleteLocalRef(env, element);
                        }
                        void* result = ${context.mangle("intarray_new")}(elements, length, true);
                        free(elements);
                        return result;
                    }
                """.trimIndent())
                castsFunctions += "static void* JNI_to_native_enum_array(JNIEnv *env, jobjectArray src);"
            }
            if (context.hasEnumArrayToKotlinCast) {
                appendLine("""
                    
                    static jobjectArray JNI_to_kotlin_enum_array(JNIEnv *env, void* src, jclass class, jmethodID cast_method, bool free) {
                        if(src == NULL) return NULL;
                        const jint length = ${context.mangle("intarray_length")}(src);
                        const int32_t* ints = ${context.mangle("intarray_elements")}(src);
                        const jobjectArray result = (*env)->NewObjectArray(env, length, class, NULL);
                        for (jint i = 0; i < length; i++)
                            (*env)->SetObjectArrayElement(env, result, i, JNI_to_kotlin_enum(env, ints[i], cast_method));
                        if (free) ${context.mangle("intarray_free")}(src);
                        return result;
                    }
                """.trimIndent())
                castsFunctions += "static jobjectArray JNI_to_kotlin_enum_array(JNIEnv *env, void* src, jclass class, jmethodID cast_method, bool free);"
            }
        }

        // Object arrays
        buildList {
            context.dictionaries.mapTo(this) { dictionary ->
                Triple(
                    dictionary.name,
                    dictionary in context.usedObjectArrayToNativeCast,
                    dictionary in context.usedObjectArrayToKotlinCast
                )
            }
            context.interfaces.mapTo(this) { inter ->
                Triple(
                    inter.name,
                    inter in context.usedObjectArrayToNativeCast,
                    inter in context.usedObjectArrayToKotlinCast
                )
            }
            add(Triple("String", context.hasStringArrayToNativeCast, context.hasStringArrayToKotlinCast))
        }.forEach { (name, hasToNative, hasToKotlin) ->
            val lower = name.camelCase().lowercase()

            if (hasToNative || hasToKotlin) appendLine("\n// Array: $name")
            if (hasToNative) {
                appendLine("""
                    
                    static void* JNI_to_native_${lower}_array(JNIEnv *env, jobjectArray src, bool nullable_elements) {
                        if(src == NULL) return NULL;
                        jsize length = (*env)->GetArrayLength(env, src);
                        void* result = ${context.mangle("array_${lower}_new")}(length, nullable_elements);
                        for(int i = 0; i < length; i++) {
                            jobject element = (*env)->GetObjectArrayElement(env, src, i);
                            ${context.mangle("array_${lower}_push")}(result, JNI_to_native_$lower(env, element), nullable_elements);
                            (*env)->DeleteLocalRef(env, element);
                        }
                        return result;
                    }
                """.trimIndent())
                castsFunctions += "static void* JNI_to_native_${lower}_array(JNIEnv *env, jobjectArray src, bool nullable_elements);"
            }
            if (hasToKotlin) {
                appendLine("""
                    
                    static jobjectArray JNI_to_kotlin_${lower}_array(JNIEnv *env, void* src, bool nullable_elements, bool free) {
                        if(src == NULL) return NULL;
                        const jint length = ${context.mangle("array_${lower}_length")}(src, nullable_elements);
    
                        const jobjectArray result = (*env)->NewObjectArray(env, length, class_$lower, NULL);
                        for (jint i = 0; i < length; i++)
                            (*env)->SetObjectArrayElement(env, result, i, JNI_to_kotlin_${lower}(env, ${context.mangle("array_${lower}_get")}(src, i, nullable_elements), false));
                        if (free) ${context.mangle("array_${lower}_free")}(src, nullable_elements);
                        return result;
                    }
                """.trimIndent())
                castsFunctions += "static jobjectArray JNI_to_kotlin_${lower}_array(JNIEnv *env, void* src, bool nullable_elements, bool free);"
            }
        }
    }

    private fun StringBuilder.printDictionaries(castsFunctions: ArrayList<String>) {
        if (!context.hasDictionaries)
            return
        printLabel("Dictionaries")

        context.dictionaries.forEach { dictionary ->
            val lower = dictionary.name.camelCase().lowercase()
            val fields = context.allFields[dictionary]!!

            if (dictionary in context.usedDeclarations)
                append("\n// ${dictionary.name}\n")

            if (dictionary in context.toNativeDeclarations) {
                append("""
                    
                    static void* JNI_to_native_$lower(JNIEnv *_env, jobject src) {
                        if(src == NULL) return NULL;
                        return ${context.mangle("${lower}_new")}(
                """.trimIndent())
                fields.joinTo(this) { field ->
                    val expr = "(*_env)->${field.type.toMethodCall(true)}(_env, _class, ${
                        field.dictionaryFieldMethod(dictionary)
                    }, src)"
                    "\n\t\t${castToNative(field.type, expr)}"
                }
                append("\n\t);\n}\n")

                castsFunctions += "static void* JNI_to_native_$lower(JNIEnv *_env, jobject src);"
            }
            if (dictionary in context.toKotlinDeclarations) {
                append("""
                    
                    static jobject JNI_to_kotlin_$lower(JNIEnv *_env, void* src, bool free) {
                        if(src == NULL) return NULL;
                        jobject result = (*_env)->CallStaticObjectMethod(_env, _class, ${dictionary.constructorMethod},
                """.trimIndent())
                fields.joinTo(this) { field ->
                    val expr = "${dictionary.subFieldCFunc(context, field)}(src)"
                    "\n\t\t${castToKotlin(field.type, expr, false)}"
                }
                appendLine("""
                    
                        );
                        if (free) ${context.mangle("${lower}_free")}(src);
                    	return result;
                    }
                """.trimIndent())
                castsFunctions += "static jobject JNI_to_kotlin_$lower(JNIEnv *_env, void* src, bool free);"
            }
        }
    }

    private fun StringBuilder.printInterfaces(castsFunctions: ArrayList<String>) {
        if (!context.hasInterfaces)
            return
        printLabel("Interfaces")

        context.interfaces.forEach { inter ->
            val lower = inter.name.camelCase().lowercase()
            if (inter in context.toNativeDeclarations) {
                appendLine("""
                    
                    static void* JNI_to_native_$lower(JNIEnv *env, jobject src) {
                        if(src == NULL) return NULL;
                        jlong ptr = (*env)->CallStaticLongMethod(env, _class, interface_ptr, src);
                        return ${context.mangle("interface_${lower}_clone")}((void*) ptr);
                    }
                """.trimIndent())
                castsFunctions += "static void* JNI_to_native_$lower(JNIEnv *env, jobject src);"
            }
            if (inter in context.toKotlinDeclarations) {
                appendLine("""
                    
                    static jobject JNI_to_kotlin_$lower(JNIEnv *env, void* src, bool free) {
                        if(src == NULL) return NULL;
                        void* cloned = ${context.mangle("interface_${lower}_clone")}(src);
                        jobject result = (void*) (*env)->CallStaticObjectMethod(env, _class, ${inter.constructorMethod}, cloned);
                        if (free) ${context.mangle("interface_${lower}_free")}(src);
                        return result;
                    }
                """.trimIndent())
                castsFunctions += "static jobject JNI_to_kotlin_$lower(JNIEnv *env, void* src, bool free);"
            }
        }
    }

    private fun StringBuilder.printCallbacks(castsFunctions: ArrayList<String>) {
        if (!context.hasCallbacks)
            return
        printLabel("Callbacks")

        appendLine("""
            
            static jint JVM_attach(JNIEnv **env) {
            	const jint status = (*jvm)->GetEnv(jvm, (void**) env, JNI_VERSION_1_6);
            	if (status == JNI_EDETACHED)
            		(*jvm)->AttachCurrentThread(jvm, (void**) env, NULL);
            	return status;
            }

            static void JVM_detach(const jint status) {
            	if (status == JNI_EDETACHED)
            		(*jvm)->DetachCurrentThread(jvm);
            }

            static bool JNI_callback_equals(void* id1, void* id2) {
                JNIEnv *_env;
                const jint status = JVM_attach(&_env);
            	bool result = (*_env)->CallStaticBooleanMethod(_env, _class, callback_equals, id1, id2);
                JVM_detach(status);
                return result;
            }

            static void JNI_callback_free(void* id1) {
                JNIEnv *_env;
                const jint status = JVM_attach(&_env);
            	(*_env)->DeleteGlobalRef(_env, (jobject) id1);
                JVM_detach(status);
            }
        """.trimIndent())

        context.callbacks.forEach { callback ->
            if (callback !in context.usedDeclarations)
                return@forEach

            val lower = callback.name.camelCase().lowercase()

            if (callback in context.toNativeDeclarations) {
                val type = callback.type.toCommonNativeType()
                val ret = if (!callback.type.isVoid()) "$type _result = " else ""

                val args = buildList {
                    add("void* _self")
                    callback.args.mapTo(this) { "${it.type.toCommonNativeType()} ${it.cname}" }
                }.joinToString()

                val castedArgs = buildList {
                    add("(jobject) _self")
                    callback.args.mapTo(this) { castToKotlin(it.type, it.cname, true) }
                }.joinToString()

                val call =
                    "(*_env)->${callback.type.toMethodCall(true)}(_env, _class, ${callback.invokeMethod}, $castedArgs)"
                val expr = castToNative(callback.type, call)

                // Invoke func
                appendLine("""
                    
                    // ${callback.name}
                    
                    static $type JNI_invoke_$lower($args) {
                        JNIEnv *_env;
                        const jint status = JVM_attach(&_env);
                        $ret$expr;
                        JVM_detach(status);
                """.trimIndent())
                if (!callback.type.isVoid())
                    appendLine("\treturn _result;")
                appendLine("}")

                // cast
                appendLine("""
                    
                    static void* JNI_to_native_$lower(JNIEnv *env, jobject src) {
                        if (src == NULL) return NULL;
                        return ${context.mangle("${lower}_new")}(
                            (size_t) (*env)->NewGlobalRef(env, src), 
                            (*env)->CallStaticIntMethod(env, _class, callback_hash_code, src),
                            (void*)&JNI_invoke_$lower, (void*)&JNI_callback_equals, (void*)*JNI_callback_free
                        );
                    }
                """.trimIndent())

                castsFunctions += "static void* JNI_to_native_$lower(JNIEnv *env, jobject src);"
            }
            if (callback in context.toKotlinDeclarations) {
                appendLine("""
                    
                    static jobject JNI_to_kotlin_$lower(JNIEnv *env, void* src, bool free) {
                        if (src == NULL) return NULL;
                        jobject result = (*env)->NewLocalRef(env, (jobject) ${context.mangle("${lower}_id")}(src));
                        if (free) ${context.mangle("${lower}_free")}(src);
                        return result;
                    }
                """.trimIndent())
                castsFunctions += "static jobject JNI_to_kotlin_$lower(JNIEnv *env, void* src, bool free);"
            }
        }
    }

    private fun StringBuilder.printFunctions() {
        if (context.allOperations.isEmpty())
            return
        printLabel("Functions")

        // Function
        context.allOperations.forEach { function ->
            val critical = function.isCritical()
            val type = function.type.toJniType()

            val args = buildList {
                add("JNIEnv *_env")
                add("jclass _")
                function.args.mapTo(this) {
                    "${it.type.toJniType()} ${it.cname}"
                }
            }.joinToString()

            val casts = function.args.mapNotNull {
                val name = it.cname
                val nullable = if (it.type.isNullable) "${it.cname} ? " else ""
                val nullableIf = if (it.type.isNullable) "if(${it.cname}) " else ""
                val nullObj = if (it.type.isNullable) " : NULL" else ""
                val nullNum = if (it.type.isNullable) " : -1" else ""
                when {
                    critical && it.type.isString() -> listOf(
                        "jbyteArray _${name}_bytes = $nullable(jbyteArray) (*_env)->CallObjectMethod(_env, $name, string_get_bytes, string_utf8_const)$nullObj;",
                        "char* _${name}_data = $nullable(*_env)->GetPrimitiveArrayCritical(_env, _${name}_bytes, JNI_FALSE)$nullObj;",
                        "jint _${name}_size = $nullable(*_env)->GetArrayLength(_env, _${name}_bytes)$nullNum;",
                        "jint _${name}_length = $nullable(*_env)->GetStringLength(_env, $name)$nullNum;",
                    )
                    critical && it.type.isEnumArray() -> listOf(
                        "jint _${name}_length = $nullable(*_env)->GetArrayLength(_env, $name)$nullNum;",
                        "int32_t* _${name}_ints = $nullable(int32_t*) malloc(_${name}_length * sizeof(int32_t))$nullObj;",
                        "${nullableIf}for (int i = 0; i < _${name}_length; i++) {",
                        "\tjobject el = (*_env)->GetObjectArrayElement(_env, $name, i);",
                        "\t_${name}_ints[i] = (*_env)->CallIntMethod(_env, el, enum_ordinal);",
                        "\t(*_env)->DeleteLocalRef(_env, el);",
                        "}"
                    )
                    critical && it.type.isArray() -> listOf(
                        "${it.type.arrayTypeOrNull()!!.toJniType()}* _${it.cname}_elements = $nullable(*_env)->GetPrimitiveArrayCritical(_env, ${it.cname}, JNI_FALSE)$nullObj;",
                        "jint _${it.cname}_length = $nullable(*_env)->GetArrayLength(_env, ${it.cname})$nullNum;",
                    )
                    else -> null
                }
            }.flatten()

            val castedArgs = function.args.joinToString {
                when {
                    critical && it.type.isString() -> "_${it.cname}_data, _${it.cname}_length, _${it.cname}_size"
                    critical && it.type.isEnumArray() -> "_${it.cname}_ints, _${it.cname}_length"
                    critical && it.type.isArray() -> {
                        val cast = if (it.type.isBooleanArray() || it.type.isUnsigned() || it.type.isLongArray())
                            "(${it.type.arrayTypeOrNull()!!.toCommonNativeType()}*) " else ""
                        "${cast}_${it.cname}_elements, _${it.cname}_length"
                    }
                    else -> castToNative(it.type, it.cname)
                }
            }

            val free = function.args.mapNotNull {
                val nullableIf = if (it.type.isNullable) "if(${it.cname}) " else ""
                when {
                    critical && it.type.isString() -> "${nullableIf}(*_env)->ReleasePrimitiveArrayCritical(_env, _${it.cname}_bytes, _${it.cname}_data, JNI_ABORT);"
                    critical && it.type.isEnumArray() -> "${nullableIf}free((void*) _${it.cname}_ints);"
                    critical && it.type.isArray() -> "${nullableIf}(*_env)->ReleasePrimitiveArrayCritical(_env, ${it.cname}, _${it.cname}_elements, JNI_ABORT);"
                    else -> null
                }
            }

            val call = castToKotlin(function.type, "${function.cnameMangled(context)}($castedArgs)", true)

            // Print

            append("\nstatic $type ${function.jniName}($args) {")

            casts.forEach { append("\n\t$it") }
            append("\n\t")
            if (!function.type.isVoid())
                append(if (free.isEmpty()) "return " else "const ${function.type.toCommonNativeType()} _result = ")
            append("$call;")
            free.forEach { append("\n\t$it") }
            if (!function.type.isVoid() && free.isNotEmpty())
                append("\n\treturn _result;")
            append("\n}")
        }
        append("\n")
    }

    private fun StringBuilder.printFunctionsList() {
        append("\nstatic void** JNI_functions(bool useCritical) {")

        if(context.allOperations.isEmpty()) {
            append("\n\treturn NULL;\n}")
            return
        }

        append("\n\tvoid** result = malloc(${context.allOperations.size} * sizeof(void*));")
        context.allOperations.forEachIndexed { i, it ->
            val func = if (isAndroidCriticalEnabled && it.isCritical() && it.isAndroidCriticalCapable())
                " useCritical ? &${it.cnameMangled(context)} : &${it.jniName}"
            else "&${it.jniName}"

            append("\n\tresult[$i] = (void*)$func;")
        }
        append("\n\treturn result;\n}")
    }

    private val ResolvedIdlDeclaration.className: String
        get() = "class_${name.camelCase().lowercase()}"

    private val ResolvedIdlEnum.castMethod: String
        get() = "method_${name.camelCase().lowercase()}_cast"

    private val ResolvedIdlDictionary.constructorMethod: String
        get() = "method_${name.camelCase().lowercase()}"

    private fun ResolvedIdlField.Declaration.dictionaryFieldMethod(dictionary: ResolvedIdlDictionary) =
        "method_${dictionary.name.camelCase().lowercase()}_${name.camelCase().lowercase()}"

    private val ResolvedIdlInterface.constructorMethod: String
        get() = "method_${name.camelCase().lowercase()}"

    private val ResolvedIdlCallbackFunction.invokeMethod: String
        get() = "method_${name.camelCase().lowercase()}"

    private val ResolvedIdlOperation.jniName: String
        get() = "PROXY_$cname"

    private fun castToKotlin(
        type: ResolvedIdlType,
        content: String,
        free: Boolean
    ): String = when {
        type.isUByte() -> "(jbyte) $content"
        type.isUShort() -> "(jshort) $content"
        type.isUInt() -> "(jint) $content"
        type.isUnsigned() -> castToKotlin(type.toSignedType(), content, free)
        type.isEnum() -> "JNI_to_kotlin_enum(_env, $content, ${(type.declaration as ResolvedIdlEnum).castMethod})"
        type.isString() -> "JNI_to_kotlin_string(_env, $content, $free)"
        type.isRawInterface() -> "(jlong) $content"
        type.isCallback() || type.isDictionary() || type.isInterface() ->
            "JNI_to_kotlin_${type.declaration.name.camelCase().lowercase()}(_env, $content, $free)"
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() -> "JNI_to_kotlin_${type.toKotlinType().lowercase()}array(_env, $content, $free)"
                type.isEnum() -> "JNI_to_kotlin_enum_array(_env, $content, ${type.declaration.className}, ${(type.declaration as ResolvedIdlEnum).castMethod}, $free)"
                type.isString() -> "JNI_to_kotlin_string_array(_env, $content, ${type.isNullable}, $free)"
                else -> "JNI_to_kotlin_${type.declaration.name.camelCase().lowercase()}_array(_env, $content, ${type.isNullable}, $free)"
            }
        }
        else -> content
    }

    private fun castToNative(
        type: ResolvedIdlType,
        content: String
    ): String = when {
        type.isUnsigned() -> castToNative(type.toSignedType(), content)
        type.isEnum() -> "JNI_to_native_enum(_env, $content)"
        type.isString() -> "JNI_to_native_string(_env, $content)"
        type.isRawInterface() -> "(void*) $content"
        type.isInterface() || type.isCallback() || type.isDictionary() ->
            "JNI_to_native_${type.declaration.name.camelCase().lowercase()}(_env, $content)"
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() -> "JNI_to_native_${type.toKotlinType().lowercase()}array(_env, $content)"
                type.isEnum() -> "JNI_to_native_enum_array(_env, $content)"
                type.isString() -> "JNI_to_native_string_array(_env, $content, ${type.isNullable})"
                else -> "JNI_to_native_${
                    type.declaration.name.camelCase().lowercase()
                }_array(_env, $content, ${type.isNullable})"
            }
        }
        else -> content
    }

    private fun ResolvedIdlType.toJniType(): String = when {
        isUnsigned() -> toSignedType().toJniType()
        isVoid() -> "void"
        isChar() -> "jchar"
        isBoolean() -> "jboolean"
        isByte() -> "jbyte"
        isShort() -> "jshort"
        isInt() -> "jint"
        isLong() || isRawInterface() -> "jlong"
        isFloat() -> "jfloat"
        isDouble() -> "jdouble"
        isString() -> "jstring"
        isArray() -> arrayType { type ->
            when {
                type.isPrimitive() -> "${type.toJniType()}Array"
                else -> "jobjectArray"
            }
        }
        else -> "jobject"
    }

    private fun ResolvedIdlType.toMethodCall(isStatic: Boolean): String {
        val static = if (isStatic) "Static" else ""
        return when {
            isUnsigned() -> toSignedType().toMethodCall(isStatic)
            isVoid() -> "Call${static}VoidMethod"
            isBoolean() -> "Call${static}BooleanMethod"
            isChar() -> "Call${static}CharMethod"
            isByte() -> "Call${static}ByteMethod"
            isShort() -> "Call${static}ShortMethod"
            isInt() -> "Call${static}IntMethod"
            isLong() -> "Call${static}LongMethod"
            isFloat() -> "Call${static}FloatMethod"
            isDouble() -> "Call${static}DoubleMethod"
            else -> "Call${static}ObjectMethod"
        }
    }
}