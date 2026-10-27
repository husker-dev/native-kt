get_filename_component(nativekt_root "${CMAKE_CURRENT_LIST_DIR}/../../.." ABSOLUTE)

# Get local 'nativekt-cmake-${version}.tar.gz' path.
file(READ "${nativekt_root}/VERSION" nativekt_version)
string(STRIP "${nativekt_version}" nativekt_version)

set(nativekt_archive "${nativekt_root}/modules/cli/build/distributions/nativekt-cmake-${nativekt_version}.tar.gz")
file(SHA256 "${nativekt_archive}" nativekt_archive_hash)

set_property(DIRECTORY APPEND PROPERTY CMAKE_CONFIGURE_DEPENDS "${nativekt_archive}")

# Load
include(FetchContent)
FetchContent_Declare(nativekt
        URL "${nativekt_archive}"
        URL_HASH "SHA256=${nativekt_archive_hash}"
)
FetchContent_MakeAvailable(nativekt)
