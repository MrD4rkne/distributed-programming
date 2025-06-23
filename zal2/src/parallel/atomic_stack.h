#ifndef ATOMIC_STACK_H
#define ATOMIC_STACK_H

#include "common/sumset.h"
#include <stdbool.h>

typedef struct stack_node {
    Sumset* dataA;
    Sumset* dataB;
    size_t i;
    Sumset* bufor;
    struct stack_node* next;
} stack_node_t;

struct atomic_stack;

typedef struct atomic_stack atomic_stack_t;

// Function to create a new stack.
_Atomic atomic_stack_t* atomic_stack_create();

// Function to push data onto the stack
void atomic_stack_push(_Atomic atomic_stack_t* stack, stack_node_t* node);

// Function to pop data from the stack
stack_node_t* atomic_stack_pop(_Atomic atomic_stack_t* stack);

// Function to check if the stack is empty
bool atomic_stack_is_empty(_Atomic atomic_stack_t* stack);

// Function to destroy the stack. Frees all the memory allocated for the stack.
void atomic_stack_free_destroy(_Atomic atomic_stack_t* stack);
#endif