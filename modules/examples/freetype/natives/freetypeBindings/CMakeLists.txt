cmake_minimum_required(VERSION 3.24)

project("freetypeBindings")

# Install: nativekt (local)
include("${CMAKE_CURRENT_LIST_DIR}/../../../../tests/natives/LocalNativeKt.cmake")

# Install: freetype
FetchContent_Declare(
        freetype
        GIT_REPOSITORY https://github.com/freetype/freetype.git
        GIT_TAG        VER-2-15-0
)
set(FT_DISABLE_ZLIB OFF CACHE INTERNAL "Disable ZLIB support")
set(FT_WITH_BZIP2 OFF CACHE INTERNAL "Disable BZIP2 support")
set(FT_WITH_PNG OFF CACHE INTERNAL "Disable PNG support (no color emoji)")
set(FT_WITH_BROTLI OFF CACHE INTERNAL "Disable Brotli support (no WOFF2)")
set(FT_WITH_HARFBUZZ OFF CACHE INTERNAL "Disable HarfBuzz support")
FetchContent_MakeAvailable(freetype)
set(MINIMAL_FT_MODULES [=[
#include <freetype/config/ftheader.h>
FT_USE_MODULE( FT_Driver_ClassRec, tt_driver_class )
FT_USE_MODULE( FT_Module_Class, sfnt_module_class )
FT_USE_MODULE( FT_Renderer_Class, ft_smooth_renderer_class )
]=])
file(WRITE "${freetype_SOURCE_DIR}/include/freetype/config/ftmodule.h" "${MINIMAL_FT_MODULES}")

# Create library
add_library(${PROJECT_NAME} STATIC src/freetypeBindings.c)

# Apply dependencies
nativekt_generate("nativekt.toml")
target_link_libraries(${PROJECT_NAME} PRIVATE NativeKt::bindings)
target_link_libraries(${PROJECT_NAME} PRIVATE freetype)