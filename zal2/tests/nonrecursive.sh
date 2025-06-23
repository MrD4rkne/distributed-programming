#!/bin/bash

#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

SRC_DIR="../src"

index="ms459531"

# Run cleanup script
echo -e "${CYAN}Running cleanup script...${NC}"
if ! ./clean.sh; then
    echo -e "${RED}Cleanup script failed.${NC}"
    exit 1
fi
echo -e "${GREEN}Cleanup script finished.${NC}"

# Run prepare script
CURRENT_DIR=$(pwd)
if ! cd ../; then
    echo -e "${RED}Error: Could not change directory.${NC}"
    exit 1
fi

echo -e "${CYAN}Running prepare script...${NC}"
if ! ./prepare.sh; then
    echo -e "${RED}Prepare script failed.${NC}"
    exit 1
fi
echo -e "${GREEN}Prepare script finished.${NC}"

zipName="${index}.zip"
zipPath=$(realpath "$zipName")
echo -e "${YELLOW}Full path to zip file: $zipPath${NC}"

if ! cd "$CURRENT_DIR"; then
    echo -e "${RED}Error: Could not change directory.${NC}"
    exit 1
fi

if ! cd "nonrecursive_time"; then
    echo -e "${RED}Error: Could not change directory to nonrecursive_time.${NC}"
    exit 1
fi

./run.sh "$zipPath"