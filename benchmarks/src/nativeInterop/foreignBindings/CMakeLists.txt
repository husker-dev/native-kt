cmake_minimum_required(VERSION 3.15)

project("foreignBindings")

add_library(${PROJECT_NAME} STATIC bindings.c)