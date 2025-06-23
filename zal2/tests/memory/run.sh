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

threads=(1 2 4 8 16 32 64)

for t in ${threads[@]}; do
    echo -e "${CYAN}Running parallel with t=${t}...${NC}"

    # Increment threadsNumber
    threadsNumber=$((t + 1))

    # Run the command with the correct thread count
    echo "$t 32 1 0 1" | prlimit --as=$((threadsNumber * 128 * 1024 * 1024)) time -f '%e seconds, %M kb (max RSS),  exit=%x' timeout --foreground 60s ./parallel/parallel
    code=$?
    if [ $code -eq 124 ]; then
        echo -e "${RED}Timeout.${NC}"
        exit 1
    fi

    if [ $code -eq 137 ]; then
        echo -e "${RED}Memory limit exceeded.${NC}"
        exit 1
    fi

    if [ $code -ne 0 ]; then
        echo -e "${RED}Error.${NC}"
        exit 1
    fi
done

echo -e "${GREEN}Success.${NC}"
exit 0