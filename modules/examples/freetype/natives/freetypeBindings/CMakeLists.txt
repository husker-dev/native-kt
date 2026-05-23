cmake_minimum_required(VERSION 3.15)

project("freetypeBindings")

add_library(${PROJECT_NAME} STATIC src/freetypeBindings.c)

if(EMSCRIPTEN)
    set(EM_FLAGS "-sUSE_FREETYPE=1")

    target_compile_options(${PROJECT_NAME} PUBLIC ${EM_FLAGS})
    target_link_options(${PROJECT_NAME} PUBLIC ${EM_FLAGS})
elseif(MINGW)
    find_package(PkgConfig REQUIRED)
    pkg_check_modules(FREETYPE REQUIRED freetype2)
    target_include_directories(${PROJECT_NAME} PRIVATE ${FREETYPE_INCLUDE_DIRS})
    target_link_libraries(${PROJECT_NAME} PRIVATE ${FREETYPE_LIBRARIES})
else()
    find_package(Freetype REQUIRED)
    target_include_directories(${PROJECT_NAME} PRIVATE ${FREETYPE_INCLUDE_DIRS})
    target_link_libraries(${PROJECT_NAME} PRIVATE Freetype::Freetype)
endif()

target_include_directories(${PROJECT_NAME} PRIVATE include)