#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

dir=$(pwd)

valgrind=0

if ! ../build.sh "$1" ; then
    echo -e "${RED}Failed to build the project.${NC}"
    exit 1
fi

cd build/

echo -e "${CYAN}Scanning for input files...${NC}"
inputs=$(find ../inputs -name "*.in" -type f)
echo -e "${CYAN}Found: ${inputs}${NC}"

for test in $inputs; do
    echo -e "${YELLOW}Running test ${test}...${NC}"

    # Parrallel
    echo -e "${CYAN}Running parallel...${NC}"
    time_file_parallel="$test.parallel.time"
    output_file_parallel="$test.parallel.out"
    parallel=1

    if [[ $valgrind -eq 1 ]]; then
        cat "$test" | time -o "$time_file_parallel" -f "%E real, %U user, %S sys" valgrind --leak-check=full --show-leak-kinds=all --track-origins=yes ./parallel/parallel > "$output_file_parallel" 2>&1
    else
        cat "$test" | time -o "$time_file_parallel" -f "%E real, %U user, %S sys" ./parallel/parallel > "$output_file_parallel"
    fi

    if [[ $? -ne 0 ]]; then
        echo -e "${RED}Parallel: FAILED${NC}"
        parallel=0
    fi

    # Reference

    echo -e "${CYAN}Running reference...${NC}"
    time_file_ref="$test.ref.time"
    output_file_ref="$test.ref.out"
    cat "$test" | time -o "$time_file_ref" -f "%E real, %U user, %S sys" ./reference/reference > "$output_file_ref"

    echo -e "${CYAN}Running nonrecursive...${NC}"
    time_file_nonrecursive="$test.nonrecursive.time"
    output_file_nonrecursive="$test.nonrecursive.out"
    nonrecursive=1

    if [[ $valgrind -eq 1 ]]; then
        cat "$test" | time -o "$time_file_nonrecursive" -f "%E real, %U user, %S sys" valgrind --leak-check=full --show-leak-kinds=all --track-origins=yes ./nonrecursive/nonrecursive > "$output_file_nonrecursive" 2>&1
    else
        cat "$test" | time -o "$time_file_nonrecursive" -f "%E real, %U user, %S sys" ./nonrecursive/nonrecursive > "$output_file_nonrecursive"
    fi

    if [[ $? -ne 0 ]]; then
        echo -e "${RED}Nonrecursive: FAILED${NC}"
        nonrecursive=0
    fi

    echo -e "${CYAN}Comparing outputs...${NC}"

    if [[ $nonrecursive -eq 1 ]]; then
        if diff "$output_file_ref" "$output_file_nonrecursive" > /dev/null; then
            echo -e "${GREEN}Nonrecursive: OK${NC}"
        else
            echo -e "${RED}Nonrecursive: wrong output${NC}"
        fi
    fi

    if [[ $parallel -eq 1 ]]; then
        if diff "$output_file_ref" "$output_file_parallel" > /dev/null; then
            echo -e "${GREEN}Parallel: OK${NC}"
        else
            echo -e "${RED}Parallel: wrong output${NC}"
        fi
    fi

done
