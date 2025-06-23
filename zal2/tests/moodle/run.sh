#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

dir=$(pwd)

if ! cp $1 ./ ; then
    echo -e "${RED}Failed to copy the project.${NC}"
    exit 1
fi

zipfile=$(basename $1)
index="${zipfile%.zip}"

unzip $zipfile
# plik ab12345/CMakeLists.txt oraz folder ab12345/common/ zostaną skopiowane z oryginalnego archiwum, nadpisując wszelkie zmiany.
cmake -S $index/ -B build/ -DCMAKE_BUILD_TYPE=Release
cd build/
make
echo -n -e '1 3 1 0\n1\n\n' | ./nonrecursive/nonrecursive
echo -n -e '1 3 1 0\n1\n\n' | ./parallel/parallel

echo -e "${CYAN}Running reference...${NC}"
reference_outfile="reference_out.txt"

echo -n -e '1 3 1 0\n1\n\n' | ./reference/reference > $reference_outfile 2>&1
if [ $? -ne 0 ]; then
    echo -e "${RED}Reference failed.${NC}"
    exit 1
fi

echo -e "${CYAN}Running nonrecursive...${NC}"
nonrecursive_outfile="nonrecursive_out.txt"

echo -n -e '1 3 1 0\n1\n\n' | ./nonrecursive/nonrecursive > $nonrecursive_outfile 2>&1
if [ $? -ne 0 ]; then
    echo -e "${RED}Nonrecursive failed.${NC}"
    exit 1
fi

echo -e "${CYAN}Running parallel...${NC}"
parallel_outfile="parallel_out.txt"

echo -n -e '1 3 1 0\n1\n\n' | ./parallel/parallel > $parallel_outfile 2>&1
if [ $? -ne 0 ]; then
    echo -e "${RED}Parallel failed.${NC}"
    exit 1
fi

echo -e "${CYAN}Comparing output...${NC}"

failed=0

if diff $reference_outfile $nonrecursive_outfile > /dev/null; then
    echo -e "${GREEN}Nonrecursive output is the same as the reference.${NC}"
else
    echo -e "${RED}Nonrecursive output is different from the reference.${NC}"
    failed=1
fi

if diff $reference_outfile $parallel_outfile > /dev/null; then
    echo -e "${GREEN}Parallel output is the same as the reference.${NC}"
else
    echo -e "${RED}Parallel output is different from the reference.${NC}"
    failed=1
fi

# Success

if [ $failed -eq 0 ]; then
    echo -e "${GREEN}All tests passed.${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed.${NC}"
    exit 1
fi
