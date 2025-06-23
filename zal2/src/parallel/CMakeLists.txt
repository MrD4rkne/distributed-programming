add_executable(parallel main.c)
target_link_libraries(parallel io err atomic)
target_sources(parallel PRIVATE atomic_stack.c)