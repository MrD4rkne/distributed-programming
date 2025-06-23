#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

dir=$(pwd)

if ! ../build.sh "$1" ; then
    echo -e "${RED}Failed to build the project.${NC}"
    exit 1
fi

cd build/

input="1 34 1 0 1"

input_file="input.txt"

if ! echo "$input" > $input_file; then
    echo -e "${RED}Failed to write input to file.${NC}"
    exit 1
fi

# Running reference
echo -e "${CYAN}Running reference...${NC}"
reference_outfile="reference_out.txt"
reference_timefile="reference_time.txt"

prlimit --as=$((128 * 1024 * 1024)) time -f"%e" ./reference/reference < "$input_file" > "$reference_outfile" 2> "$reference_timefile"

if [ $? -ne 0 ]; then
    echo -e "${RED}Reference failed.${NC}"
    exit 1
fi


# Running nonrecursive
echo -e "${CYAN}Running nonrecursive...${NC}"
nonrecursive_outfile="nonrecursive_out.txt"
nonrecursive_timefile="nonrecursive_time.txt"

prlimit --as=$((128 * 1024 * 1024)) time -f"%e" ./nonrecursive/nonrecursive < "$input_file" > "$nonrecursive_outfile" 2> "$nonrecursive_timefile"

if [ $? -ne 0 ]; then
    echo -e "${RED}Nonrecursive failed.${NC}"
    exit 1
fi


failed=0

if diff $reference_outfile $nonrecursive_outfile > /dev/null; then
    echo -e "${GREEN}Nonrecursive output is the same as the reference.${NC}"
else
    echo -e "${RED}Nonrecursive output is different from the reference.${NC}"
    failed=1
fi

reference_time=$(cat "$reference_timefile")
nonrecursive_time=$(cat "$nonrecursive_timefile")

# Print execution times
echo -e "${YELLOW}Reference execution time:${NC}"
echo $reference_time

echo -e "${YELLOW}Nonrecursive execution time:${NC}"
echo $nonrecursive_time

time_ratio=$(echo "$nonrecursive_time / $reference_time" | bc -l)

echo -e "${YELLOW}Time ratio:${NC} $time_ratio"

threshold=1.5
ratio_comparison=$(echo "$time_ratio > $threshold" | bc)

if [ "$ratio_comparison" -eq 1 ]; then
    echo -e "${RED}Nonrecursive is more than 1.5 times slower than the reference.${NC}"
    failed=1
fi

# Success
if [ $failed -ne 0 ]; then
    echo -e "${RED}Some tests failed.${NC}"
    exit 1
else
    echo -e "${GREEN}All tests passed.${NC}"
    exit 0
fi
