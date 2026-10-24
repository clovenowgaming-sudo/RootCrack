cmake_minimum_required(VERSION 3.22.1)
project("rootcrack_native")

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_compile_options(
    -Wall -fvisibility=hidden -fvisibility-inlines-hidden
    -ffunction-sections -fdata-sections -fno-rtti
)
add_link_options(-Wl,--gc-sections -Wl,--build-id=none)

add_library(rootcrack_native SHARED native_bridge.cpp)
target_link_libraries(rootcrack_native PRIVATE log)

add_custom_command(TARGET rootcrack_native POST_BUILD
    COMMAND ${CMAKE_STRIP} --strip-all $<TARGET_FILE:rootcrack_native>)
