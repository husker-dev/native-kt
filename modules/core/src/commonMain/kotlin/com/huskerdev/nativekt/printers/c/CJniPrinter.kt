package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.kotlin.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import com.huskerdev.webidl.*
import io.github.vinceglb.filekit.*

class CJniPrinter(
    val context: NativeModuleContext,
    target: PlatformFile,
    headerTarget: PlatformFile,
    val isAndroid: Boolean
) {
    private val isAndroidCriticalEnabled =
        isAndroid && (context.configuration as? NativeKtAndroidConfiguration)?.useAndroidCriticalNative ?: false

    init {
        val castsFunctions = arrayListOf<String>()
        target.writeSync(buildString {
            append("#include \"${headerTarget.name}\"\n\n")
            printBasicCasts(castsFunctions)
            printDictionaries(castsFunctions)
            printInterfaces(castsFunctions)
            printCallbacks(castsFunctions)
            printRegister()
            printFunctions()
            printFunctionsList()
        })
        headerTarget.writeSync(buildString {
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

        // Boxed primitives
        listOf(
            Triple("char", context.hasCharNullableToNative, context.hasCharNullableToKotlin),
            Triple("boolean", context.hasBooleanNullableToNative, context.hasBooleanNullableToKotlin),
            Triple("byte",
                context.hasByteNullableToNative || context.hasUByteNullableToNative,
                context.hasByteNullableToKotlin || context.hasUByteNullableToKotlin),
            Triple("short",
                context.hasShortNullableToNative || context.hasUShortNullableToNative,
                context.hasShortNullableToKotlin || context.hasUShortNullableToKotlin),
            Triple("int",
                context.hasIntNullableToNative || context.hasUIntNullableToNative || context.hasEnumNullableToNative,
                context.hasIntNullableToKotlin || context.hasUIntNullableToKotlin || context.hasEnumNullableToKotlin),
            Triple("long",
                context.hasLongNullableToNative || context.hasULongNullableToNative,
                context.hasLongNullableToKotlin || context.hasULongNullableToKotlin),
            Triple("float", context.hasFloatNullableToNative, context.hasFloatNullableToKotlin),
            Triple("double", context.hasDoubleNullableToNative, context.hasDoubleNullableToKotlin)
        ).forEach { (name, hasToNativeCast, hasToKotlinCast) ->
            if(!hasToNativeCast && !hasToKotlinCast) return@forEach

            classes += "class_${name}"
            if(hasToNativeCast) methods += "${name}_get"
            if(hasToKotlinCast) methods += "${name}_new"
        }

        // String
        if (context.hasString)
            classes += "class_string"

        // Dictionaries
        if(context.hasDictionary) {
            context.castedDictionaries.forEach { dictionary ->
                if (dictionary in context.toKotlinDeclaration) {
                    classes += dictionary.className
                    methods += dictionary.constructorMethod
                }
                if (dictionary in context.toNativeDeclaration) {
                    context.allFields[dictionary]!!.forEach { field ->
                        methods += field.dictionaryFieldMethod(dictionary)
                    }
                }
            }
        }

        // Interfaces
        if(context.hasInterface) {
            if (context.hasInterfaceToNative)
                methods += "interface_ptr"
            context.castedInterfaces.forEach { inter ->
                if (inter in context.toKotlinDeclaration)
                    classes += inter.className
                if (inter in context.toNativeDeclaration)
                    methods += inter.constructorMethod
            }
        }

        // Callbacks
        if (context.hasCallback) {
            methods += "callback_equals"
            methods += "callback_hash_code"
            context.castedCallbacks.forEach { callback ->
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
        printLabel("Registration")

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

        if(context.hasPrimitiveNullable || context.hasEnumNullable) {
            appendLine("\t// Get boxed primitives")

            listOf(
                Triple("Character" to "C", context.hasCharNullableToNative, context.hasCharNullableToKotlin),
                Triple("Boolean" to "Z", context.hasBooleanNullableToNative, context.hasBooleanNullableToKotlin),
                Triple("Byte" to "B",
                    context.hasByteNullableToNative || context.hasUByteNullableToNative,
                    context.hasByteNullableToKotlin || context.hasUByteNullableToKotlin),
                Triple("Short" to "S",
                    context.hasShortNullableToNative || context.hasUShortNullableToNative,
                    context.hasShortNullableToKotlin || context.hasUShortNullableToKotlin),
                Triple("Integer" to "I",
                    context.hasIntNullableToNative || context.hasUIntNullableToNative || context.hasEnumNullableToNative,
                    context.hasIntNullableToKotlin || context.hasUIntNullableToKotlin || context.hasEnumNullableToKotlin),
                Triple("Long" to "J",
                    context.hasLongNullableToNative || context.hasULongNullableToNative,
                    context.hasLongNullableToKotlin || context.hasULongNullableToKotlin),
                Triple("Float" to "F", context.hasFloatNullableToNative, context.hasFloatNullableToKotlin),
                Triple("Double" to "D", context.hasDoubleNullableToNative, context.hasDoubleNullableToKotlin)
            ).forEach { (names, hasToNativeCast, hasToKotlinCast) ->
                if(!hasToNativeCast && !hasToKotlinCast) return@forEach
                val (name, d) = names
                val lowerName = when (name) {
                    "Character" -> "char"
                    "Integer" -> "int"
                    else -> name.lowercase()
                }

                append("\n\tclass_$lowerName = (*env)->NewGlobalRef(env, (*env)->FindClass(env, \"java/lang/$name\"));")
                if(hasToNativeCast)
                    append("\n\t${lowerName}_new = (*env)->GetStaticMethodID(env, class_$lowerName, \"valueOf\", \"($d)Ljava/lang/$name;\");")
                if(hasToKotlinCast)
                    append("\n\t${lowerName}_get = (*env)->GetMethodID(env, class_$lowerName, \"${lowerName}Value\", \"()$d\");")
            }
            append("\n")
        }

        // String
        if (context.hasString) {
            appendLine("""
                
                // String
                class_string = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "[B"));
            """.replaceIndent("\t"))
        }

        // Dictionaries
        if (context.hasDictionary) {
            append("\n\t// Dictionaries")
            context.castedDictionaries.forEach { dictionary ->
                if (dictionary in context.toKotlinDeclaration) {
                    append("\n\t${dictionary.className} = $nextClass;")
                    append("\n\t${dictionary.constructorMethod} = $nextFunc;")
                }
                if (dictionary in context.toNativeDeclaration) {
                    context.allFields[dictionary]!!.forEach { field ->
                        append("\n\t${field.dictionaryFieldMethod(dictionary)} = $nextFunc;")
                    }
                }
            }
            append("\n")
        }

        // Interfaces
        if (context.hasInterface) {
            append("\n\t// Interfaces")
            if (context.hasInterfaceToNative)
                append("\n\tinterface_ptr = $nextFunc;")
            context.castedInterfaces.forEach { inter ->
                if (inter in context.toKotlinDeclaration) append("\n\t${inter.className} = $nextClass;")
                if (inter in context.toNativeDeclaration) append("\n\t${inter.constructorMethod} = $nextFunc;")
            }
            append("\n")
        }

        // Callbacks
        if (context.hasCallback) {
            append("\n\t// Callbacks")
            append("\n\tcallback_equals = $nextFunc;")
            append("\n\tcallback_hash_code = $nextFunc;")
            context.castedCallbacks.forEach { callback ->
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
            if (context.hasStringToNative) {
                appendLine("""
                    
                    static void* JNI_to_native_string_sized(JNIEnv *env, jbyteArray obj, jsize size) {
                        if(obj == NULL) return NULL;
                        jbyte* data = NULL;
                        if(size > 0) {
                            data = (jbyte*) ${context.mangle("alloc")}(size);
                            
                            if (size > 1024) {
                                jbyte* raw = (jbyte*) (*env)->GetPrimitiveArrayCritical(env, obj, JNI_FALSE);
                                memcpy(data, raw, (size_t) size);
                                (*env)->ReleasePrimitiveArrayCritical(env, obj, raw, JNI_ABORT);
                            } else (*env)->GetByteArrayRegion(env, obj, 0, size, data);
                        }
                        return ${context.mangle("string_new")}((const char*) data, size, false);
                    }
                    
                    static void* JNI_to_native_string(JNIEnv *env, jbyteArray obj) {
                        if(obj == NULL) return NULL;
                        return JNI_to_native_string_sized(env, obj, (*env)->GetArrayLength(env, obj));
                    }
                """.trimIndent())
                castsFunctions += "static void* JNI_to_native_string(JNIEnv *env, jstring obj);"
            }
            if (context.hasStringToKotlin) {
                appendLine("""
                    
                    static jbyteArray JNI_to_kotlin_string(JNIEnv *env, void* str, bool free) {
                        if(str == NULL) return NULL;
    
                        jint size = (jint) ${context.mangle("string_size")}(str);
                        jbyteArray result = (*env)->NewByteArray(env, size);
                        if(size > 0)
                            (*env)->SetByteArrayRegion(env, result, 0, size, (jbyte*) ${context.mangle("string_data")}(str));
                        
                        if (free) ${context.mangle("string_free")}(str);
                        return result;
                    }
                """.trimIndent())
                castsFunctions += "static jstring JNI_to_kotlin_string(JNIEnv *env, void* str, bool free);"
            }
        }

        if(context.hasPrimitiveNullable || context.hasEnumNullable) {
            listOf(
                Triple("Char" to "uint16_t", context.hasCharNullableToNative, context.hasCharNullableToKotlin),
                Triple("Boolean" to "int8_t", context.hasBooleanNullableToNative, context.hasBooleanNullableToKotlin),
                Triple("Byte" to "int8_t",
                    context.hasByteNullableToNative || context.hasUByteNullableToNative,
                    context.hasByteNullableToKotlin || context.hasUByteNullableToKotlin),
                Triple("Short" to "int16_t",
                    context.hasShortNullableToNative || context.hasUShortNullableToNative,
                    context.hasShortNullableToKotlin || context.hasUShortNullableToKotlin),
                Triple("Int" to "int32_t",
                    context.hasIntNullableToNative || context.hasUIntNullableToNative || context.hasEnumNullableToNative,
                    context.hasIntNullableToKotlin || context.hasUIntNullableToKotlin || context.hasEnumNullableToKotlin),
                Triple("Long" to "int64_t",
                    context.hasLongNullableToNative || context.hasULongNullableToNative,
                    context.hasLongNullableToKotlin || context.hasULongNullableToKotlin),
                Triple("Float" to "float", context.hasFloatNullableToNative, context.hasFloatNullableToKotlin),
                Triple("Double" to "double", context.hasDoubleNullableToNative, context.hasDoubleNullableToKotlin),
            ).forEach { (names, hasToNative, hasToKotlin) ->
                val (name, type) = names
                val lower = name.lowercase()

                appendLine("\n// $name")
                if(hasToNative) appendLine("""
                    
                    static void* JNI_to_native_${lower}(JNIEnv *env, jobject obj) {
                        if(obj == NULL) return NULL;
                        $type value = ($type) (*env)->Call${name}Method(env, obj, ${lower}_get);
                        return ${context.mangle("${lower}_new")}(value);
                    }
                """.trimIndent())
                if(hasToKotlin) appendLine("""
                    
                    static jobject JNI_to_kotlin_${lower}(JNIEnv *env, void* ptr, const bool free) {
                        if(ptr == NULL) return NULL;
                        $type value = ${context.mangle("${lower}_get")}(ptr, free);
                        return (*env)->CallStaticObjectMethod(env, class_${lower}, ${lower}_new, value);
                    }
                """.trimIndent())
            }
        }

        // Primitive arrays
        listOf(
            Triple("char", context.hasCharArrayToNative, context.hasCharArrayToKotlin),
            Triple("boolean", context.hasBooleanArrayToNative, context.hasBooleanArrayToKotlin),
            Triple("byte",
                context.hasByteArrayToNative || context.hasUByteArrayToNative,
                context.hasByteArrayToKotlin || context.hasUByteArrayToKotlin),
            Triple("short",
                context.hasShortArrayToNative || context.hasUShortArrayToNative,
                context.hasShortArrayToKotlin || context.hasUShortArrayToKotlin),
            Triple("int",
                context.hasIntArrayToNative || context.hasUIntArrayToNative || context.hasEnumArrayToNative,
                context.hasIntArrayToKotlin || context.hasUIntArrayToKotlin || context.hasEnumArrayToKotlin),
            Triple("long",
                context.hasLongArrayToNative || context.hasULongArrayToNative,
                context.hasLongArrayToKotlin || context.hasULongArrayToKotlin),
            Triple("float", context.hasFloatArrayToNative, context.hasFloatArrayToKotlin),
            Triple("double", context.hasDoubleArrayToNative, context.hasDoubleArrayToKotlin),
        ).forEach { (name, hasToNative, hasToKotlin) ->
            val capitalized = name.uppercaseFirstChar()

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
                
                    static void* JNI_to_native_${name}array_sized(JNIEnv *env, const j${name}Array arr, jsize length) {
                        if(arr == NULL) return NULL;
                        
                        size_t size = length * sizeof(j$name);
                        j$name* elements = NULL;
                        if(size > 0) {
                            elements = (j$name*) ${context.mangle("alloc")}(size);
    
                            if (size > 1024) {
                                j$name* raw = (j$name*) (*env)->GetPrimitiveArrayCritical(env, arr, NULL);
                                memcpy(elements, raw, size);
                                (*env)->ReleasePrimitiveArrayCritical(env, arr, raw, JNI_ABORT);
                            } else (*env)->Get${capitalized}ArrayRegion(env, arr, 0, length, elements);
                        }
                        return ${context.mangle("${name}array_new")}(${kCast}elements, length, false);
                    }
                    
                    static void* JNI_to_native_${name}array(JNIEnv *env, const j${name}Array arr) {
                        if(arr == NULL) return NULL;
                        return JNI_to_native_${name}array_sized(env, arr, (*env)->GetArrayLength(env, arr));
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
                        if(length > 0)
                            (*env)->Set${capitalized}ArrayRegion(env, result, 0, length, $jCast${context.mangle("${name}array_elements")}(arr));
                        if (free) ${context.mangle("${name}array_free")}(arr);
                        return result;
                    }
                """.trimIndent())
                castsFunctions += "static j${name}Array JNI_to_kotlin_${name}array(JNIEnv *env, void* arr, const bool free);"
            }
        }

        // Object arrays
        buildList {
            context.dictionaries.mapTo(this) { dictionary ->
                Triple(
                    dictionary.name,
                    dictionary in context.usedObjectArrayToNative,
                    dictionary in context.usedObjectArrayToKotlin
                )
            }
            context.interfaces.mapTo(this) { inter ->
                Triple(
                    inter.name,
                    inter in context.usedObjectArrayToNative,
                    inter in context.usedObjectArrayToKotlin
                )
            }
            add(Triple("String", context.hasStringArrayToNative, context.hasStringArrayToKotlin))
            add(Triple("Char", context.hasCharNullableArrayToNative, context.hasCharNullableArrayToKotlin))
            add(Triple("Boolean", context.hasBooleanNullableArrayToNative, context.hasBooleanNullableArrayToKotlin))
            add(Triple("Byte",
                context.hasByteNullableArrayToNative || context.hasUByteNullableArrayToNative,
                context.hasByteNullableArrayToKotlin || context.hasUByteNullableArrayToKotlin))
            add(Triple("Short",
                context.hasShortNullableArrayToNative || context.hasUShortNullableArrayToNative,
                context.hasShortNullableArrayToKotlin || context.hasUShortNullableArrayToKotlin))
            add(Triple("Int",
                context.hasIntNullableArrayToNative || context.hasUIntNullableArrayToNative || context.hasEnumNullableArrayToNative,
                context.hasIntNullableArrayToKotlin || context.hasUIntNullableArrayToKotlin || context.hasEnumNullableArrayToKotlin))
            add(Triple("Long",
                context.hasLongNullableArrayToNative || context.hasULongNullableArrayToNative,
                context.hasLongNullableArrayToKotlin || context.hasULongNullableArrayToKotlin))
            add(Triple("Float", context.hasFloatNullableArrayToNative, context.hasFloatNullableArrayToKotlin))
            add(Triple("Double", context.hasDoubleNullableArrayToNative, context.hasDoubleNullableArrayToKotlin))
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
        if (!context.hasDictionary)
            return
        printLabel("Dictionaries")

        context.dictionaries.forEach { dictionary ->
            val lower = dictionary.name.camelCase().lowercase()
            val fields = context.allFields[dictionary]!!

            if (dictionary in context.castedDeclarations)
                append("\n// ${dictionary.name}\n")

            if (dictionary in context.toNativeDeclaration) {
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
            if (dictionary in context.toKotlinDeclaration) {
                append("""
                    
                    static jobject JNI_to_kotlin_$lower(JNIEnv *_env, void* src, bool free) {
                        if(src == NULL) return NULL;
                        jobject result = (*_env)->CallStaticObjectMethod(
                """.trimIndent())

                buildList {
                    add("_env")
                    add("_class")
                    add(dictionary.constructorMethod)
                    fields.mapTo(this) { field ->
                        val expr = "${dictionary.subFieldCFunc(context, field)}(src)"
                        "\n\t\t${castToKotlin(field.type, expr, false)}"
                    }
                }.joinTo(this)

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
        if (!context.hasInterface)
            return
        printLabel("Interfaces")

        context.interfaces.forEach { inter ->
            val lower = inter.name.camelCase().lowercase()
            if (inter in context.toNativeDeclaration) {
                appendLine("""
                    
                    static void* JNI_to_native_$lower(JNIEnv *env, jobject src) {
                        if(src == NULL) return NULL;
                        jlong ptr = (*env)->CallStaticLongMethod(env, _class, interface_ptr, src);
                        return ${context.mangle("interface_${lower}_clone")}((void*) ptr);
                    }
                """.trimIndent())
                castsFunctions += "static void* JNI_to_native_$lower(JNIEnv *env, jobject src);"
            }
            if (inter in context.toKotlinDeclaration) {
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
        if (!context.hasCallback)
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
            if (callback !in context.castedDeclarations)
                return@forEach

            val lower = callback.name.camelCase().lowercase()

            if (callback in context.toNativeDeclaration) {
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
            if (callback in context.toKotlinDeclaration) {
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
                    when {
                        it.type.isString() -> "${it.type.toJniType()} ${it.cname}, jint _${it.cname}_size"
                        it.type.isArray() -> "${it.type.toJniType()} ${it.cname}, jint _${it.cname}_length"
                        else -> "${it.type.toJniType()} ${it.cname}"
                    }
                }
            }.joinToString()

            val casts = function.args.mapNotNull {
                val name = it.cname
                when {
                    critical && it.type.isString() -> """
                        char* _${name}_data = NULL;
                        if(_${name}_size > 1024) {
                            _${name}_data = (*_env)->GetPrimitiveArrayCritical(_env, $name, JNI_FALSE);
                        } else if(_${name}_size > 0) {
                            _${name}_data = alloca(_${name}_size);
                            (*_env)->GetByteArrayRegion(_env, $name, 0, _${name}_size, (jbyte*) _${name}_data);
                        }
                    """.trimIndent().split("\n")
                    critical && it.type.isEnumArray() -> """
                        jsize _${name}_size = _${it.cname}_length * sizeof(jint);
                        jint* _${it.cname}_elements = NULL;
                        if(_${name}_size > 1024) {
                            _${name}_elements = (*_env)->GetPrimitiveArrayCritical(_env, $name, JNI_FALSE);
                        } else if(_${name}_size > 0) {
                            _${name}_elements = alloca(_${name}_size);
                            (*_env)->GetIntArrayRegion(_env, $name, 0, _${name}_length, _${name}_elements);
                        }
                    """.trimIndent().split("\n")
                    critical && it.type.isArray() -> {
                        val arrType = it.type.arrayTypeOrNull()!!
                        val jtype = arrType.toJniType()
                        """
                        jsize _${name}_size = _${it.cname}_length * sizeof($jtype);
                        $jtype* _${it.cname}_elements = NULL;
                        if(_${name}_size > 1024) {
                            _${name}_elements = (*_env)->GetPrimitiveArrayCritical(_env, $name, JNI_FALSE);
                        } else if(_${name}_size > 0) {
                            _${name}_elements = alloca(_${name}_size);
                            (*_env)->Get${arrType.toKotlinType(ignoreUnsigned = true)}ArrayRegion(_env, $name, 0, _${name}_length, _${name}_elements);
                        }
                        """.trimIndent().split("\n")
                    }
                    else -> null
                }
            }.flatten()

            val castedArgs = function.args.joinToString {
                when {
                    critical && it.type.isString() -> "_${it.cname}_data, _${it.cname}_size"
                    critical && it.type.isArray() -> {
                        val cast = if (it.type.isBooleanArray() || it.type.isUnsigned() || it.type.isLongArray())
                            "(${it.type.arrayTypeOrNull()!!.toCommonNativeType()}*) " else ""
                        "${cast}_${it.cname}_elements, _${it.cname}_length"
                    }
                    it.type.isString() -> castToNative(it.type, it.cname, size = "_${it.cname}_size")
                    it.type.isArray() -> castToNative(it.type, it.cname, size = "_${it.cname}_length")
                    else -> castToNative(it.type, it.cname)
                }
            }

            val free = function.args.mapNotNull {
                when {
                    critical && it.type.isString() ->
                        "if(_${it.cname}_size > 1024) (*_env)->ReleasePrimitiveArrayCritical(_env, ${it.cname}, _${it.cname}_data, JNI_ABORT);"
                    critical && (it.type.isPrimitiveArray() || it.type.isEnumArray()) ->
                        "if(_${it.cname}_size > 1024) (*_env)->ReleasePrimitiveArrayCritical(_env, ${it.cname}, _${it.cname}_elements, JNI_ABORT);"
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
        type.isPrimitive(isNullable = true) -> {
            val lower = type.toKotlinType(ignoreUnsigned = true, printNullable = false).lowercase()
            "JNI_to_kotlin_${lower}(_env, (j$lower*) $content, $free)"
        }
        type.isEnum() ->
            if(type.isNullable) "JNI_to_kotlin_int(_env, $content, $free)"
            else content
        type.isUByte() -> "(jbyte) $content"
        type.isUShort() -> "(jshort) $content"
        type.isUInt() -> "(jint) $content"
        type.isPrimitive() && type.isUnsigned() -> castToKotlin(type.toSignedType(), content, free)
        type.isString() -> "JNI_to_kotlin_string(_env, $content, $free)"
        type.isRawInterface() -> "(jlong) $content"
        type.isCallback() || type.isDictionary() || type.isInterface() ->
            "JNI_to_kotlin_${type.declaration.name.camelCase().lowercase()}(_env, $content, $free)"
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive(isNullable = false) -> "JNI_to_kotlin_${type.toKotlinType(ignoreUnsigned = true).lowercase()}array(_env, $content, $free)"
                type.isEnum() ->
                    if(type.isNullable) "JNI_to_kotlin_int_array(_env, $content, true, $free)"
                    else "JNI_to_kotlin_intarray(_env, $content, $free)"
                type.isString() -> "JNI_to_kotlin_string_array(_env, $content, ${type.isNullable}, $free)"
                else -> "JNI_to_kotlin_${type.toKotlinType(printNullable = false, ignoreUnsigned = true).lowercase()}_array(_env, $content, ${type.isNullable}, $free)"
            }
        }
        else -> content
    }

    private fun castToNative(
        type: ResolvedIdlType,
        content: String,
        size: String? = null
    ): String = when {
        type.isPrimitive(isNullable = true) ->
            "(${type.toCommonNativeType()}) JNI_to_native_${type.toKotlinType(ignoreUnsigned = true, printNullable = false).lowercase()}(_env, $content)"
        type.isEnum() ->
            if(type.isNullable) "JNI_to_native_int(_env, $content)"
            else content
        type.isPrimitive() && type.isUnsigned() -> castToNative(type.toSignedType(), content)
        type.isString() ->
            if(size == null) "JNI_to_native_string(_env, $content)"
            else "JNI_to_native_string_sized(_env, $content, $size)"
        type.isRawInterface() -> "(void*) $content"
        type.isInterface() || type.isCallback() || type.isDictionary() ->
            "JNI_to_native_${type.declaration.name.camelCase().lowercase()}(_env, $content)"
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive(isNullable = false) ->
                    if(size == null) "JNI_to_native_${type.toKotlinType(ignoreUnsigned = true).lowercase()}array(_env, $content)"
                    else "JNI_to_native_${type.toKotlinType(ignoreUnsigned = true).lowercase()}array_sized(_env, $content, $size)"
                type.isEnum() ->
                    if(type.isNullable) "JNI_to_native_int_array(_env, $content, true)"
                    else "JNI_to_native_intarray(_env, $content)"
                type.isString() -> "JNI_to_native_string_array(_env, $content, ${type.isNullable})"
                else -> "JNI_to_native_${type.toKotlinType(printNullable = false, ignoreUnsigned = true).lowercase()}_array(_env, $content, ${type.isNullable})"
            }
        }
        else -> content
    }

    private fun ResolvedIdlType.toJniType(): String = when {
        (isPrimitive() || isEnum()) && isNullable -> "jobject"
        isPrimitive() && isUnsigned() -> toSignedType().toJniType()
        isVoid() -> "void"
        isChar() -> "jchar"
        isBoolean() -> "jboolean"
        isByte() -> "jbyte"
        isShort() -> "jshort"
        isInt() || isEnum() -> "jint"
        isLong() || isRawInterface() -> "jlong"
        isFloat() -> "jfloat"
        isDouble() -> "jdouble"
        isString() -> "jbyteArray"
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
            isPrimitive() && isUnsigned() -> toSignedType().toMethodCall(isStatic)
            isVoid() -> "Call${static}VoidMethod"
            isBoolean() -> "Call${static}BooleanMethod"
            isChar() -> "Call${static}CharMethod"
            isByte() -> "Call${static}ByteMethod"
            isShort() -> "Call${static}ShortMethod"
            isInt() || isEnum() -> "Call${static}IntMethod"
            isLong() -> "Call${static}LongMethod"
            isFloat() -> "Call${static}FloatMethod"
            isDouble() -> "Call${static}DoubleMethod"
            else -> "Call${static}ObjectMethod"
        }
    }
}