#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

dir=$(pwd)

echo -e "${CYAN}Building DEBUG...${NC}"

if ! ../build.sh "$1" Debug ; then
    echo -e "${RED}Failed to build the project.${NC}"
    exit 1
fi

cd build/
echo -e "${CYAN}Running and expecting to fail${NC}"

echo '8 32 1 0 1' | prlimit --as=$((9*128*1024*1024)) time -f'%e seconds, %M kb (max RSS),  exit=%x' timeout --foreground 10s gdb --batch -ex "run" -ex "bt" --args ./parallel/parallel > release.out 2>&1
if [ $? -eq 0 ]; then
    echo -e "${RED}Expected to fail.${NC}"
    exit 1
fi



