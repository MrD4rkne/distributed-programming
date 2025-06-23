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

echo -e "${CYAN}Running perf...${NC}"
for i in {64,32,16,8,4,2,1} ; do
    echo -e "${CYAN}Running perf for ${i} threads...${NC}"
    echo "${i} 32 1 0 1" | timeout --foreground 180s time ./parallel/parallel
    if [ $? -ne 0 ] ; then
        echo -e "${RED}Failed to run perf for ${i} threads.${NC}"
        exit 1
    fi
done