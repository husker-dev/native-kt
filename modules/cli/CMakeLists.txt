cmake_minimum_required(VERSION 3.24)

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

    # Find CLI executable
    file(GLOB tools LIST_DIRECTORIES false "${CMAKE_CURRENT_FUNCTION_LIST_DIR}/nativekt-cli-*-${platform}${extension}")
    list(LENGTH tools tool_count)
    if(NOT tool_count EQUAL 1)
        message(FATAL_ERROR "Expected one Native-Kt executable for ${platform}, found ${tool_count}")
    endif()
    list(GET tools 0 tool)

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
        message(FATAL_ERROR "Native-Kt failed to resolve the NDL path (${ndl_result})\n${ndl_stderr}")
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

    # Mark triggers
    set_property(DIRECTORY APPEND PROPERTY CMAKE_CONFIGURE_DEPENDS "${tool}" "${config}" "${ndl_file}")

    # Collect sources and include files
    file(GLOB_RECURSE sources LIST_DIRECTORIES false CONFIGURE_DEPENDS "${sources_dir}/*")
    file(GLOB_RECURSE headers LIST_DIRECTORIES false CONFIGURE_DEPENDS "${include_dir}/*")

    # Create library
    # Keep bindings local so sibling projects can generate their own configuration.
    add_library(NativeKt::bindings INTERFACE IMPORTED)

    target_sources(NativeKt::bindings INTERFACE ${sources} ${headers})
    target_include_directories(NativeKt::bindings INTERFACE "${include_dir}")
endfunction()
