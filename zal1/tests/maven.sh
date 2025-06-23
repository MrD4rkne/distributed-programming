#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Step 1: Check if Maven is installed
if ! command -v mvn &>/dev/null; then
    echo -e "${RED}Error: Maven is not installed.${NC}"
    exit 1
fi
echo -e "${GREEN}Maven is installed.${NC}"

# Step 2: Navigate to the root directory where pom.xml is located
echo -e "${BLUE}Step 2: Navigating to the root directory...${NC}"
ROOT_DIR="../"  # Adjust to point to the root directory relative to the script location
cd "$ROOT_DIR" || { echo -e "${RED}Error: Unable to navigate to the root directory.${NC}"; exit 1; }

# Step 3: Run the tests using Maven
echo -e "${BLUE}Step 3: Running tests using Maven...${NC}"

# Run Maven test command from the root directory where pom.xml is located
mvn test
if [ $? -ne 0 ]; then
    echo -e "${RED}Error: Tests failed.${NC}"
    exit 1
fi

echo -e "${GREEN}Tests executed successfully.${NC}"

# Step 4: Clean up target files
echo -e "${YELLOW}Cleaning up target files...${NC}"
mvn clean
echo -e "${GREEN}Clean up completed.${NC}"
