#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

dir=$(pwd)

if ! ../build.sh "$1" Debug ; then
    echo -e "${RED}Failed to build the project.${NC}"
    exit 1
fi

cd build/

echo -e "${CYAN}Scanning for input files...${NC}"
inputs=$(find ../inputs -name "*.in" -type f)
echo -e "${CYAN}Found: ${inputs}${NC}"

failed=0

for test in $inputs; do
    pass=1

    echo -e "${YELLOW}Running test ${test}...${NC}"

    # Parrallel
    echo -e "${CYAN}Running parallel...${NC}"
    time_file_parallel="$test.parallel.time"
    output_file_parallel="$test.parallel.out"
    parallel=1

    cat "$test" | time -o "$time_file_parallel" -f "%E real, %U user, %S sys" valgrind --error-exitcode=1 --leak-check=full --show-leak-kinds=all --track-origins=yes ./parallel/parallel > "$output_file_parallel" 2>&1

    if [[ $? -ne 0 ]]; then
        echo -e "${RED}Parallel: FAILED${NC}"
        cat "$output_file_parallel"
        parallel=0
        pass=0
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

    cat "$test" | time -o "$time_file_nonrecursive" -f "%E real, %U user, %S sys" valgrind --error-exitcode=1 --leak-check=full --show-leak-kinds=all --track-origins=yes ./nonrecursive/nonrecursive > "$output_file_nonrecursive" 2>&1
    if [[ $? -ne 0 ]]; then
        echo -e "${RED}Nonrecursive: FAILED${NC}"
        cat "$output_file_nonrecursive"
        nonrecursive=0
        pass=0
    fi

    if [[ $pass -eq 1 ]]; then
        echo -e "${GREEN}PASSED${NC}"
    else
        failed=($failed + 1)
    fi

done

# REPORT

echo -e "${CYAN}Generating report...${NC}"
echo -e "Failed: ${RED}${failed}${NC}"
if [[ $failed -eq 0 ]]; then
    echo -e "${GREEN}All tests passed.${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed.${NC}"
    exit 1
fi
