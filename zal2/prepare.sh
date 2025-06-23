#!/bin/bash

# Define color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Set variables
USER_ID="ms459531" # Replace with your initials and student number
OUTPUT_ZIP="${USER_ID}.zip"
SRC_DIR="./src"
OUTPUT_DIR="${USER_ID}"

# Check if should generate new raport

refreshRaport=false

while getopts ":r" opt; do
    case ${opt} in
        r )
            refreshRaport=true
            ;;
        \? )
            echo "Usage: prepare.sh [-r]"
            echo "Options:"
            echo "  -r    Refresh the raport"
            exit 1
            ;;
    esac
done

# Check if raport exists
raportPath="./tests/.raport/report.pdf"
if [ ! -f "$raportPath" ]; then
    refreshRaport=true
fi

# Check if the source directory exists
if [ ! -d "$SRC_DIR" ]; then
    echo -e "${RED}Error: Source directory '$SRC_DIR' does not exist.${NC}"
    exit 1
fi

# Prepare output directory
echo -e "${CYAN}Preparing submission directory...${NC}"
rm -rf "$OUTPUT_DIR"  # Remove existing folder if it exists
mkdir -p "$OUTPUT_DIR"

# Copy source files into appropriate folders
echo -e "${YELLOW}Copying source files...${NC}"
cp -r "$SRC_DIR/"* "$OUTPUT_DIR/"

# Replace files which should not be edited
zipWithFiles="ab12345.zip"
echo -e "${YELLOW}Replacing ./src files with original ones...${NC}"
echo -e "${CYAN}Unzipping ${zipWithFiles}...${NC}"
if ! unzip -oq "${zipWithFiles}" ; then
    echo -e "${RED}Failed to unzip ${replacedZip}.${NC}"
    exit 1
fi

tempFolder=$(basename "${zipWithFiles}" .zip)
echo -e "${CYAN}Copying files from ${tempFolder} to ${OUTPUT_DIR}...${NC}"

extractedFolder=$(unzip -Z -1 "${zipWithFiles}" | head -n 1)
if ! cd "${extractedFolder}" ; then
    echo -e "${RED}Failed to change directory to ${extractedFolder}.${NC}"
    exit 1
fi

if ! cp -r ./ "../${OUTPUT_DIR}/" ; then
    echo -e "${RED}Failed to copy files from ${zipWithFiles} to ${OUTPUT_DIR}.${NC}"
    exit 1
fi

if ! cd .. ; then
    echo -e "${RED}Failed to navigate to original directory${NC}"
    exit 1
fi

# Remove the extracted folder
rm -rf "$tempFolder"

# Include report
if [ "$refreshRaport" = true ]; then
    echo -e "${YELLOW}Refreshing the raport...${NC}"
    echo -e "${CYAN}Generating raport...${NC}"
    if ! cd tests/.raport ; then
        echo -e "${RED}Failed to navigate to tests/.raport.${NC}"
        exit 1
    fi

    if ! ./run.sh "../../${USER_ID}.zip" ; then
        echo -e "${RED}Failed to run ./run.sh.${NC}"
        exit 1
    fi

    if ! cd ../.. ; then
        echo -e "${RED}Failed to navigate to the original directory.${NC}"
        exit 1
    fi
fi

if ! cp "$raportPath" "${OUTPUT_DIR}/report.pdf" ; then
    echo -e "${RED}Failed to copy the raport.${NC}"
    exit 1
fi

# Create the zip archive
echo -e "${CYAN}Creating zip archive '$OUTPUT_ZIP'...${NC}"
rm -f "$OUTPUT_ZIP"  # Remove existing zip file if it exists
zip -r "$OUTPUT_ZIP" "$OUTPUT_DIR" > /dev/null

# Clean up temporary folder
echo -e "${YELLOW}Cleaning up temporary files...${NC}"
rm -rf "$OUTPUT_DIR"

# Confirmation message
echo -e "${GREEN}Submission archive '$OUTPUT_ZIP' created successfully!${NC}"
