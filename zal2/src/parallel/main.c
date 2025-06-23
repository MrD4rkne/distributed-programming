#include <pthread.h>
#include <stdatomic.h>
#include <stddef.h>
#include <stdlib.h>

#include "atomic_stack.h"
#include "common/err.h"
#include "common/io.h"
#include "common/sumset.h"

#ifdef DEBUG
#define EXIT_WITH_ERROR(msg)                 \
    do {                                     \
        fprintf(stderr, "Error: %s\n", msg); \
        exit(1);                             \
    } while (0)

#define ASSERT_OK(expr)                                                                    \
    do {                                                                                   \
        if ((expr) != 0)                                                                   \
            syserr(                                                                        \
                "System command failed: %s\n\tIn function %s() in %s line %d.\n\tErrno: ", \
                #expr, __func__, __FILE__, __LINE__);                                      \
    } while (0)
#else
#define EXIT_WITH_ERROR(msg) exit(1)

#define ASSERT_OK(expr)  \
    do {                 \
        if ((expr) != 0) \
            exit(1);     \
    } while (0)
#endif

#define PER 500000
#define STACK_SIZE 128

typedef struct {
    InputData* input_data;
    _Atomic atomic_stack_t* stack;
    bool solution_found;
    int waiting_threads;
    atomic_int tasks;
    pthread_mutex_t mutex;

} ThreadArgs;

static inline void copy_buffor(const Sumset* a, Sumset* bufor, size_t i)
{
    while (a != NULL) {
        bufor[i] = *a;
        if (a->prev != NULL) {
            bufor[i].prev = &bufor[i + 1];
        }
        a = a->prev;

        ++i;
    }
}

static inline int count_path(const Sumset* a)
{
    int count = 0;
    while (a->prev != NULL) {
        ++count;
        a = a->prev;
    }

    return ++count;
}

static inline stack_node_t* copy_path(Sumset* a, Sumset* b)
{
    stack_node_t* node = malloc(sizeof(stack_node_t));
    if (node == NULL) {
        EXIT_WITH_ERROR("Failed to allocate memory for StackElement.");
    }

    size_t sizeA = count_path(a);
    size_t sizeB = count_path(b);

    node->bufor = malloc((sizeA + sizeB) * sizeof(Sumset));
    if (node->bufor == NULL) {
        EXIT_WITH_ERROR("Failed to allocate memory for bufor.");
    }

    copy_buffor(a, node->bufor, 0);
    node->dataA = &node->bufor[0];

    copy_buffor(b, node->bufor, sizeA);
    node->dataB = &node->bufor[sizeA];

    return node;
}

typedef struct {
    Sumset* a;
    Sumset* b;
    size_t i;
} StackElement;

static void solve(StackElement* stack, Sumset* sumset_pool, ThreadArgs* args, Solution* localBestSolution)
{
    int count = PER - 1;

    int d = 0;
    int left = 0;
    while (left <= d) {
        StackElement* current = &(stack[d]);
        bool decrease = true;

        for (size_t i = current->i; i <= args->input_data->d; ++i) {
            ++count;

            if (does_sumset_contain(current->b, i)) {
                continue; // Skip if sumset contains the element.
            }

            current->i = i + 1;
            StackElement* next = &(stack[d + 1]);
            Sumset* local = &(sumset_pool[d + 1]);

            sumset_add(local, current->a, i);

            // Reorder to keep smaller sumset in `a`.
            next->a = local;
            next->b = current->b;
            if (next->a->sum > next->b->sum) {
                Sumset* tmp = next->a;
                next->a = next->b;
                next->b = tmp;
            }

            next->i = next->a->last;

            if (!is_sumset_intersection_trivial(next->a, next->b)) {
                if (next->b->sum > localBestSolution->sum && next->a->sum == next->b->sum && get_sumset_intersection_size(next->a, next->b) == 2) {
                    solution_build(localBestSolution, args->input_data, next->a, next->b);
                }
            } else {
                if (count > PER) {
                    count = 0;

                    // That is not atomic, because there is no need.
                    // If no one is waiting, then somebody will take care of it in the future.
                    // Maybe even this thread.
                    if (args->tasks < STACK_SIZE) {
                        stack_node_t* elem = copy_path(stack[left].a, stack[left].b);
                        elem->i = stack[left].i;
                        atomic_stack_push(args->stack, elem);

                        ++left;
                        ++args->tasks;
                    }
                }

                ++d;
                decrease = false;
                break;
            }
        }

        if (decrease) {
            --d;
        }
    }
}

static void* compute(void* arg)
{
    ThreadArgs* args = (ThreadArgs*)arg;

    Solution* localBestSolution = malloc(sizeof(Solution));
    if (localBestSolution == NULL) {
        EXIT_WITH_ERROR("Failed to allocate memory for localBestSolution.");
    }

    solution_init(localBestSolution);

    int maxDepth = 2 * args->input_data->d * (args->input_data->d - 1);
    StackElement* stack = malloc(maxDepth * sizeof(StackElement));
    Sumset* sumset_pool = malloc(maxDepth * sizeof(Sumset));
    if (stack == NULL || sumset_pool == NULL) {
        EXIT_WITH_ERROR("Failed to allocate memory for stack or sumset pool.");
    }

    while (true) {
        // Try get from global stack.
        stack_node_t* element = atomic_stack_pop(args->stack);

        if (element == NULL) {
            ASSERT_OK(pthread_mutex_lock(&args->mutex));
            if (args->waiting_threads == args->input_data->t - 1 && atomic_stack_is_empty(args->stack)) {
                // Found solution. Wake up all threads.
                args->solution_found = true;
                ASSERT_OK(pthread_mutex_unlock(&args->mutex));
                break;
            } else {
                ++args->waiting_threads;
                ASSERT_OK(pthread_mutex_unlock(&args->mutex));

                // Wait until there is something on the stack or solution is found.
                while (element == NULL && !args->solution_found) {
                    element = atomic_stack_pop(args->stack);
                }

                // If thread was woken up because solution was found.
                if (args->solution_found) {
                    break;
                }

                ASSERT_OK(pthread_mutex_lock(&args->mutex));
            }

            --args->waiting_threads;
            ASSERT_OK(pthread_mutex_unlock(&args->mutex));
        }

        --args->tasks;

        Sumset* a = element->dataA;
        Sumset* b = element->dataB;

        stack[0].a = a;
        stack[0].b = b;
        stack[0].i = element->i;

        solve(stack, sumset_pool, args, localBestSolution);

        free(element->bufor);
        free(element);
    }

    free(stack);
    free(sumset_pool);

    pthread_exit(localBestSolution);
}

int main()
{
    InputData input_data;
    input_data_read(&input_data);

    Solution best_solution;
    solution_init(&best_solution);

    if (input_data.a_start.sum > best_solution.sum && input_data.a_start.sum == input_data.b_start.sum && !is_sumset_intersection_trivial(&input_data.a_start, &input_data.b_start) && get_sumset_intersection_size(&input_data.a_start, &input_data.b_start) == 2) {
        solution_build(&best_solution, &input_data, &input_data.a_start, &input_data.b_start);
    } else {

        // Init args.
        _Atomic atomic_stack_t* globalStack = atomic_stack_create();
        if (globalStack == NULL) {
            EXIT_WITH_ERROR("Failed to create global stack.");
        }

        ThreadArgs* args = malloc(sizeof(ThreadArgs));
        if (args == NULL) {
            EXIT_WITH_ERROR("Failed to allocate memory for args.");
        }

        args->input_data = &input_data;
        args->stack = globalStack;
        args->solution_found = false;
        args->waiting_threads = 0;

        ASSERT_OK(pthread_mutex_init(&args->mutex, NULL));

        // Seed root element.
        stack_node_t* element = malloc(sizeof(stack_node_t));
        if (element == NULL) {
            EXIT_WITH_ERROR("Failed to allocate memory for StackElement.");
        }

        element->bufor = (Sumset*)malloc(2 * sizeof(Sumset));
        if (element->bufor == NULL) {
            EXIT_WITH_ERROR("Failed to allocate memory for bufor.");
        }

        element->dataA = &element->bufor[0];
        *element->dataA = input_data.a_start;

        element->dataB = &element->bufor[1];
        *element->dataB = input_data.b_start;

        if (element->dataA->sum > element->dataB->sum) {
            Sumset* tmp = element->dataA;
            element->dataA = element->dataB;
            element->dataB = tmp;
        }

        element->i = element->dataA->last;
        atomic_stack_push(globalStack, element);

        args->tasks = 1;

        pthread_t threads[input_data.t];
        for (size_t i = 0; i < input_data.t; ++i) {
            ASSERT_OK(pthread_create(&threads[i], NULL, compute, args));
        }

        for (size_t i = 0; i < input_data.t; ++i) {
            void* thread_result;
            ASSERT_OK(pthread_join(threads[i], &thread_result));

            // Update best solution.
            Solution* localBestSolution = (Solution*)thread_result;
            if (localBestSolution->sum > best_solution.sum) {
                best_solution = *localBestSolution;
            }
            free(localBestSolution);
        }

        ASSERT_OK(pthread_mutex_destroy(&args->mutex));

        free(args);

        assert(atomic_stack_is_empty(globalStack));
        atomic_stack_free_destroy(globalStack);
    }

    solution_print(&best_solution);
    return 0;
}
