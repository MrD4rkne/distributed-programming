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

echo -e "${CYAN}Compiling generator.cpp...${NC}"

if ! g++ -march=native -O3 generator.cpp -o ./build/generator ; then
    echo -e "${RED}Failed to compile generator.cpp.${NC}"
    exit 1
fi

echo -e "${GREEN}Success${NC}"

cd build/

# Create or open timing log file
LOG_FILE="timing.log"
> "$LOG_FILE"  # Clear the log file before starting

i=0

while true; do
    i=$((i + 1))

    echo
    echo -e "${YELLOW}Running test ${i}...${NC}"

    echo -e "${CYAN}Running generator${NC}"

    input_file="input$i.in"

    if ! ./generator > "$input_file"; then
        echo -e "${RED}Failed to run generator.${NC}"
        exit 1
    fi
    success=1

    echo >> $LOG_FILE
    echo "Test $i" >> $LOG_FILE
    cat $input_file >> $LOG_FILE

    test="test$i"

    # Parrallel
    echo -e "${CYAN}Running parallel...${NC}"
    time_file_parallel="$test.parallel.time"
    output_file_parallel="$test.parallel.out"
    parallel=1

    if [[ $valgrind -eq 1 ]]; then
        cat $input_file | time -o "$time_file_parallel" -f "%E real, %U user, %S sys" valgrind --leak-check=full --show-leak-kinds=all --track-origins=yes ./parallel/parallel > "$output_file_parallel" 2>&1
    else
        cat $input_file | time -o "$time_file_parallel" -f "%E real, %U user, %S sys" ./parallel/parallel > "$output_file_parallel"
    fi

    if [[ $? -ne 0 ]]; then
        echo -e "${RED}Parallel: FAILED${NC}"
        parallel=0
    fi

    # Append timing info to the log file
    if [[ -f "$time_file_parallel" ]]; then
        echo "Parallel time for test $i:" >> "$LOG_FILE"
        cat "$time_file_parallel" >> "$LOG_FILE"
    fi

    # Reference

    echo -e "${CYAN}Running reference...${NC}"
    time_file_ref="$test.ref.time"
    output_file_ref="$test.ref.out"
    cat $input_file | time -o "$time_file_ref" -f "%E real, %U user, %S sys" ./reference/reference > "$output_file_ref"

    # Append timing info to the log file
    if [[ -f "$time_file_ref" ]]; then
        echo "Reference time for test $i:" >> "$LOG_FILE"
        cat "$time_file_ref" >> "$LOG_FILE"
    fi

    echo -e "${CYAN}Running nonrecursive...${NC}"
    time_file_nonrecursive="$test.nonrecursive.time"
    output_file_nonrecursive="$test.nonrecursive.out"
    nonrecursive=1

    if [[ $valgrind -eq 1 ]]; then
        cat $input_file | time -o "$time_file_nonrecursive" -f "%E real, %U user, %S sys" valgrind --leak-check=full --show-leak-kinds=all --track-origins=yes ./nonrecursive/nonrecursive > "$output_file_nonrecursive" 2>&1
    else
        cat $input_file | time -o "$time_file_nonrecursive" -f "%E real, %U user, %S sys" ./nonrecursive/nonrecursive > "$output_file_nonrecursive"
    fi

    if [[ $? -ne 0 ]]; then
        echo -e "${RED}Nonrecursive: FAILED${NC}"
        nonrecursive=0
    fi

    # Append timing info to the log file
    if [[ -f "$time_file_nonrecursive" ]]; then
        echo "Nonrecursive time for test $i:" >> "$LOG_FILE"
        cat "$time_file_nonrecursive" >> "$LOG_FILE"
    fi

    echo -e "${CYAN}Comparing outputs...${NC}"

    if [[ $nonrecursive -eq 1 ]]; then
        if diff "$output_file_ref" "$output_file_nonrecursive" > /dev/null; then
            echo -e "${GREEN}Nonrecursive: OK${NC}"
        else
            echo -e "${RED}Nonrecursive: wrong output${NC}"
            success=0
        fi
    fi

    if [[ $parallel -eq 1 ]]; then
        if diff "$output_file_ref" "$output_file_parallel" > /dev/null; then
            echo -e "${GREEN}Parallel: OK${NC}"
        else
            echo -e "${RED}Parallel: wrong output${NC}"
            success=0
        fi
    fi

    # Set success to 0 if any of the tests failed
    if [[ $nonrecursive -eq 0 || $parallel -eq 0 ]]; then
        success=0
    fi

    if [[ $success -eq 1 ]]; then
        echo -e "${GREEN}Test $i: OK${NC}"
        rm -f "$input_file" "$output_file_ref" "$output_file_nonrecursive" "$output_file_parallel" "$time_file_ref" "$time_file_nonrecursive" "$time_file_parallel"
    else
        echo -e "${RED}Test $i: FAILED${NC}"
    fi

done
