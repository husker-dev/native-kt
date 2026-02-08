cmake_minimum_required(VERSION 3.15)

project("jniBindings")

add_library(${PROJECT_NAME} STATIC bindings.c)