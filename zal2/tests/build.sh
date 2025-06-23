#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

dir=$(pwd)

# Check if the source folder argument is provided
if [ "$#" -lt 1 ]; then
    echo -e "${RED}Error: Please provide the source zip as an argument.${NC}"
    exit 1
fi

SOURCE_ZIP=$(basename "$1")
if ! cp "$1" "${SOURCE_ZIP}" ; then
    echo -e "${RED}Failed to copy ${1} to ${SOURCE_ZIP}.${NC}"
    exit 1
fi
echo -e "${CYAN}Source zip: ${SOURCE_ZIP}${NC}"

build_type="Release"

if [ "$#" -eq 2 ]; then
    build_type="$2"
fi

echo -e "${CYAN}Build type: ${build_type}${NC}"

echo

index=$(basename "$SOURCE_ZIP" .zip)
echo -e "${CYAN}Index: ${index}${NC}"

# Remove old files
echo -e "${YELLOW}Removing old files...${NC}"
echo
rm -rf "build/"
rm -rf "${index}/"

# Unzip index.zip
echo -e "${CYAN}Unzipping ${index}.zip...${NC}"
if ! unzip "${index}.zip" ; then
    echo -e "${RED}Failed to unzip ${index}.zip.${NC}"
    exit 1
fi

echo -e "${GREEN}Files prepared!${NC}"

echo -e "${YELLOW}Building and running the program...${NC}"

cmake -S "$index"/ -B build/ -DCMAKE_BUILD_TYPE="$build_type"
cd build/
make
echo