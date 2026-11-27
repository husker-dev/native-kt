cmake_minimum_required(VERSION 3.24)

option(NATIVEKT_MANGLE "Mangle Native-Kt C implementation names" OFF)

function(nativekt_generate config_file)

    # Validate
    if(NOT ARGC EQUAL 1)
        message(FATAL_ERROR "Usage: nativekt_generate(path/to/nativekt.toml)")
    endif()

    if(TARGET NativeKt::bindings)
        message(FATAL_ERROR "Native-Kt bindings have already been configured in this directory or a parent directory")
    endif()

    # Build CLI executable name
    string(TOLOWER "${CMAKE_HOST_SYSTEM_PROCESSOR}" arch)
    set(extension "")

    if(CMAKE_HOST_SYSTEM_NAME STREQUAL "Windows" AND arch MATCHES "^(amd64|x86_64)$")
        set(platform "windows-x64")
        set(extension ".exe")
    elseif(CMAKE_HOST_SYSTEM_NAME STREQUAL "Windows" AND arch MATCHES "^(arm64|aarch64)$")
        set(platform "windows-x64") # Windows ARM64 runs the x64 executable through emulation.
        set(extension ".exe")
    elseif(CMAKE_HOST_SYSTEM_NAME STREQUAL "Darwin" AND arch MATCHES "^(arm64|aarch64)$")
        set(platform "macos-arm64")
    elseif(CMAKE_HOST_SYSTEM_NAME STREQUAL "Linux" AND arch MATCHES "^(amd64|x86_64)$")
        set(platform "linux-x64")
    elseif(CMAKE_HOST_SYSTEM_NAME STREQUAL "Linux" AND arch MATCHES "^(arm64|aarch64)$")
        set(platform "linux-arm64")
    else()
        message(FATAL_ERROR "Unsupported Native-Kt host: ${CMAKE_HOST_SYSTEM_NAME}/${arch}")
    endif()

    # CLI executable
    set(tool "${CMAKE_CURRENT_FUNCTION_LIST_DIR}/bin/nativekt-cli-${platform}${extension}")

    # Get absolute configuration path
    get_filename_component(config "${config_file}" ABSOLUTE BASE_DIR "${CMAKE_CURRENT_SOURCE_DIR}")
    if(NOT EXISTS "${config}" OR IS_DIRECTORY "${config}")
        message(FATAL_ERROR "Native-kt configuration not found: ${config}")
    endif()

    # Getting .ndl file path
    execute_process(
            COMMAND "${tool}" print-ndl-path --config "${config}"
            WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}"
            RESULT_VARIABLE ndl_result
            OUTPUT_VARIABLE ndl_file
            ERROR_VARIABLE ndl_stderr
            OUTPUT_STRIP_TRAILING_WHITESPACE
    )
    if(NOT "${ndl_result}" STREQUAL "0")
        message(FATAL_ERROR "Native-Kt failed to resolve the NDL path (${ndl_result})\nCLI: ${tool}\nConfig: ${config}\n${ndl_stderr}")
    endif()

    # Execute CLI
    set(output_dir "${CMAKE_CURRENT_BINARY_DIR}/nativekt")
    set(include_dir "${output_dir}/include")
    set(sources_dir "${output_dir}/sources")
    file(MAKE_DIRECTORY "${include_dir}" "${sources_dir}")

    execute_process(
            COMMAND "${tool}" generate --config "${config}" --output "${output_dir}"
            WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}"
            RESULT_VARIABLE cli_result
            OUTPUT_VARIABLE cli_stdout
            ERROR_VARIABLE cli_stderr
    )
    if(NOT "${cli_result}" STREQUAL "0")
        message(FATAL_ERROR "Native-Kt generation failed (${cli_result})\n" "${cli_stdout}\n${cli_stderr}")
    endif()

    # Allow IDE integrations to discover the configuration and generated files.
    file(WRITE "${output_dir}/config.txt" "${config}")

    # Get operation name mappings
    execute_process(
            COMMAND "${tool}" print-function-mapping --config "${config}"
            WORKING_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}"
            RESULT_VARIABLE operations_result
            OUTPUT_VARIABLE operations
            ERROR_VARIABLE operations_stderr
            OUTPUT_STRIP_TRAILING_WHITESPACE
    )
    if(NOT "${operations_result}" STREQUAL "0")
        message(FATAL_ERROR "Native-Kt failed to resolve operations (${operations_result})\nCLI: ${tool}\nConfig: ${config}\n${operations_stderr}")
    endif()
    string(REPLACE "\r\n" "\n" operations "${operations}")
    string(REPLACE "\n" ";" operations "${operations}")

    # Mark triggers
    set_property(DIRECTORY APPEND PROPERTY CMAKE_CONFIGURE_DEPENDS "${tool}" "${config}" "${ndl_file}")

    # Collect sources and include files
    file(GLOB_RECURSE sources LIST_DIRECTORIES false CONFIGURE_DEPENDS "${sources_dir}/*")
    file(GLOB_RECURSE headers LIST_DIRECTORIES false CONFIGURE_DEPENDS "${include_dir}/*")

    # Create library
    # Keep bindings local so sibling projects can generate their own configuration.
    add_library(NativeKt::bindings INTERFACE IMPORTED)

    set_property(TARGET NativeKt::bindings PROPERTY NATIVEKT_OPERATIONS "${operations}")
    target_sources(NativeKt::bindings INTERFACE ${sources} ${headers})
    target_include_directories(NativeKt::bindings INTERFACE "${include_dir}")
endfunction()

function(nativekt_configure target_lib config_file)

    nativekt_generate("${config_file}")
    target_link_libraries(${target_lib} PRIVATE NativeKt::bindings)
    target_compile_options(${target_lib} PRIVATE -Wno-initializer-overrides)
    set_target_properties(${target_lib} PROPERTIES POSITION_INDEPENDENT_CODE ON)

    if(NATIVEKT_MANGLE)
        get_target_property(operations NativeKt::bindings NATIVEKT_OPERATIONS)
        foreach(operation IN LISTS operations)
            if(NOT operation STREQUAL "")
                string(APPEND operation "__impl")
                target_compile_definitions(${target_lib} PRIVATE "$<$<COMPILE_LANGUAGE:C>:${operation}>")
            endif()
        endforeach()
    endif()

    file(WRITE "${CMAKE_CURRENT_BINARY_DIR}/nativekt/target.txt" ${target_lib})

    if(EMSCRIPTEN)
        # Create executable target
        add_executable(nativekt_${target_lib}_executable $<TARGET_OBJECTS:${target_lib}>)
        target_link_libraries(nativekt_${target_lib}_executable PUBLIC ${target_lib})
    else ()
        # Create static target
        add_library(nativekt_${target_lib}_static STATIC $<TARGET_OBJECTS:${target_lib}>)
        target_link_libraries(nativekt_${target_lib}_static PUBLIC ${target_lib})

        # Create dynamic target
        add_library(nativekt_${target_lib}_shared SHARED $<TARGET_OBJECTS:${target_lib}>)
        target_link_libraries(nativekt_${target_lib}_shared PUBLIC ${target_lib})
    endif ()
endfunction()
