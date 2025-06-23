#include <stddef.h>
#include <stdlib.h>
#include <string.h>

#include "common/err.h"
#include "common/io.h"
#include "common/sumset.h"

static InputData input_data;
static Solution best_solution;

typedef struct {
    const Sumset* a;
    const Sumset* b;
    size_t i;
} StackElement;

void extend_if_necessary(StackElement** stack, Sumset** sumset_pool, int depth, int* size)
{
    if (++depth < *size) {
        return;
    }

    *size *= 2;
    *stack = realloc(*stack, *size * sizeof(StackElement));
    *sumset_pool = realloc(*sumset_pool, *size * sizeof(Sumset));

    if (*stack == NULL || *sumset_pool == NULL) {
        fatal("Failed to allocate memory for stack or sumset pool.");
    }
}

static void solve(const Sumset* c, const Sumset* e)
{
    int maxDepth = 2 * input_data.d * (input_data.d - 1);
    StackElement* stack = malloc(maxDepth * sizeof(StackElement));
    Sumset* sumset_pool = malloc(maxDepth * sizeof(Sumset));
    if (stack == NULL || sumset_pool == NULL) {
        exit(1);
    }

    // Initialize first stack element
    stack[0].a = (c->sum > e->sum) ? e : c;
    stack[0].b = (c->sum > e->sum) ? c : e;
    stack[0].i = stack[0].a->last;

    if (stack[0].a->sum > best_solution.sum && stack[0].a->sum == stack[0].b->sum && !is_sumset_intersection_trivial(stack[0].a, stack[0].b) && get_sumset_intersection_size(stack[0].a, stack[0].b) == 2) {
        solution_build(&best_solution, &input_data, stack[0].a, stack[0].b);
        free(stack);
        free(sumset_pool);
        return;
    }

    int d = 0;
    while (d >= 0) {
        StackElement* current = &stack[d];
        bool decrease = true;

        for (size_t i = current->i; i <= input_data.d; ++i) {
            if (does_sumset_contain(current->b, i)) {
                continue; // Skip if sumset contains the element
            }

            current->i = i + 1;
            StackElement* next = &stack[d + 1];
            Sumset* local = &sumset_pool[d + 1];

            sumset_add(local, current->a, i);

            // Reorder to keep smaller sumset in `a`
            next->a = local;
            next->b = current->b;
            if (next->a->sum > next->b->sum) {
                const Sumset* tmp = next->a;
                next->a = next->b;
                next->b = tmp;
            }

            next->i = next->a->last;

            if (!is_sumset_intersection_trivial(next->a, next->b)) {
                if (next->b->sum > best_solution.sum && next->a->sum == next->b->sum && get_sumset_intersection_size(next->a, next->b) == 2) {
                    solution_build(&best_solution, &input_data, next->a, next->b);
                }
            } else {
                ++d;
                decrease = false;
                break;
            }
        }

        if (decrease) {
            --d;
        }
    }

    free(stack);
    free(sumset_pool);
}

int main()
{
    input_data_read(&input_data);

    solution_init(&best_solution);
    solve(&input_data.a_start, &input_data.b_start); // Start the process with the initial sumsets
    solution_print(&best_solution);

    return 0;
}
