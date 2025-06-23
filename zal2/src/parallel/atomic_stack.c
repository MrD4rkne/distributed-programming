// Based on: https://moodle.mimuw.edu.pl/pluginfile.php/315283/mod_page/content/33/lockfree-stack.c

#include "atomic_stack.h"
#include <errno.h>
#include <stdatomic.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>

struct atomic_stack {
    uint64_t tag; // https://en.wikipedia.org/wiki/ABA_problem
    stack_node_t* head;
};

_Atomic atomic_stack_t* atomic_stack_create()
{
    atomic_stack_t* stack = malloc(sizeof(atomic_stack_t));
    if (stack == NULL) {
        errno = ENOMEM;
        return NULL;
    }

    stack->head = NULL;
    stack->tag = 0;
    return (_Atomic atomic_stack_t*)stack;
}

void atomic_stack_push(_Atomic atomic_stack_t* stack, stack_node_t* node)
{
    atomic_stack_t next;
    atomic_stack_t prev = atomic_load(stack);
    do {
        node->next = prev.head;
        next.head = node;
        next.tag = prev.tag + 1;
    } while (!atomic_compare_exchange_weak(stack, &prev, next));
}

stack_node_t* atomic_stack_pop(_Atomic atomic_stack_t* stack)
{
    atomic_stack_t next;
    atomic_stack_t prev = atomic_load(stack);
    do {
        if (prev.head == NULL) {
            return NULL; // Stack is empty
        }

        next.head = prev.head->next;
        next.tag = prev.tag + 1;
    } while (!atomic_compare_exchange_weak(stack, &prev, next));

    return prev.head;
}

bool atomic_stack_is_empty(_Atomic atomic_stack_t* stack)
{
    atomic_stack_t loaded_stack = atomic_load(stack);
    return loaded_stack.head == NULL;
}

void atomic_stack_free_destroy(_Atomic atomic_stack_t* stack)
{
    atomic_stack_t loaded_stack = atomic_load(stack);
    stack_node_t* node = loaded_stack.head;
    while (node != NULL) {
        stack_node_t* next = node->next;
        free(node);
        node = next;
    }
    free(stack);
}
