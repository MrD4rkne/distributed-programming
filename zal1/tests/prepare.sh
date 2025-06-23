#!/bin/bash

temp_dir="./temp"
files_dir="../src/main/java/cp2024"
index="ms459531"

# Clean up the bin directory
if ! ./clean.sh; then
    echo "Failed to clean ./bin"
    exit 1
fi

# Create necessary directories
if ! mkdir -p "${temp_dir}/${index}/src"; then
    echo "Failed to create ${temp_dir}/${index}/src"
    exit 1
fi

# Copy files to the temporary directory
if ! cp -r "${files_dir}" "${temp_dir}/${index}/src"; then
    echo "Failed to copy files"
    exit 1
fi

# Change to the temp directory and zip without "temp" in paths
cd "${temp_dir}" || exit 1
if ! zip -r "../${index}.zip" "${index}"; then
    echo "Failed to zip"
    exit 1
fi
cd - || exit 1

# Clean up the bin directory again
if ! ./clean.sh; then
    echo "Failed to clean ./bin"
    exit 1
fi

echo "Archive ${index}.zip created successfully!"
