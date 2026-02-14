cmake_minimum_required(VERSION 3.15)

project("jvmciBindings")

add_library(${PROJECT_NAME} STATIC bindings.c)