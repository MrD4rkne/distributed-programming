#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}Step 1: Running prepare.sh...${NC}"
./prepare.sh
if [ $? -ne 0 ]; then
    echo -e "${RED}Error: prepare.sh failed to execute.${NC}"
    exit 1
fi
echo -e "${GREEN}prepare.sh completed successfully.${NC}"

# Step 2: Unzip the created archive directly into the current directory
ZIP_FILE="ms459531.zip"
echo -e "${BLUE}Step 2: Unzipping ${ZIP_FILE} into the current directory...${NC}"

unzip -q "$ZIP_FILE"
if [ $? -ne 0 ]; then
    echo -e "${RED}Error: Failed to unzip ${ZIP_FILE}.${NC}"
    exit 1
fi
echo -e "${GREEN}Unzipping completed successfully.${NC}"

# Step 3: Compile Java files
echo -e "${BLUE}Step 3: Compiling Java files...${NC}"

# Change to the source directory
cd ms459531/src || { echo -e "${RED}Failed to change directory to ms459531/src.${NC}"; exit 1; }

# Compile Java files and run the demo
javac -d ../bin/ cp2024/*/*.java
if [ $? -ne 0 ]; then
    echo -e "${RED}Error: Compilation failed.${NC}"
    exit 1
fi

echo -e "${GREEN}Compilation completed successfully.${NC}"

# Step 4: Run the demo
echo -e "${BLUE}Step 4: Running the demo...${NC}"

java --class-path ../bin/ cp2024.demo.Demo
if [ $? -ne 0 ]; then
    echo -e "${RED}Error: Demo execution failed.${NC}"
    exit 1
fi
echo -e "${GREEN}Demo executed successfully.${NC}"

# Cleanup
echo -e "${YELLOW}Cleaning up temporary files...${NC}"
rm -rf ms459531
echo -e "${GREEN}Temporary files removed. Test completed.${NC}"
