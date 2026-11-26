get_filename_component(nativekt_root "${CMAKE_CURRENT_LIST_DIR}/../../.." ABSOLUTE)

set(nativekt_archive "${nativekt_root}/modules/cli/build/nativekt-cmake")
set_property(DIRECTORY APPEND PROPERTY CMAKE_CONFIGURE_DEPENDS "${nativekt_archive}")

# Load
include(FetchContent)
FetchContent_Declare(nativekt
        SOURCE_DIR ${nativekt_archive}
)
FetchContent_MakeAvailable(nativekt)
