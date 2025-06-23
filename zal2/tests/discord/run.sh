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

if ! cd tests ; then
    echo -e "${RED}Failed to enter the tests directory.${NC}"
    exit 1
fi

chmod +x ./test_correctness.sh
chmod +x ./timer.sh

if [ ! -d ./corretness_output ]; then
    if ! mkdir ./corretness_output ; then
        echo -e "${RED}Failed to create the output directory.${NC}"
        exit 1
    fi
fi

if ! ./timer.sh ; then
    echo -e "${RED}Failed the timer test.${NC}"
    exit 1
fi

if ! ./test_correctness.sh ; then
    echo -e "${RED}Failed the correctness test.${NC}"
    exit 1
fi

echo -e "${GREEN}All tests passed.${NC}"

exit 0