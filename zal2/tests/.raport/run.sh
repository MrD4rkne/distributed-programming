#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

dir=$(pwd)

if ! ../build.sh "$1" Release; then
    echo -e "${RED}Failed to build the project.${NC}"
    exit 1
fi

cd build/

inputSuffix="0 1 1"

d_list=(5 10 15 20 25 30 32 34)

threads_list=(1 2 4 8 16)

# Create a file to log the timing information
timing_log="timing_log.txt"
reference_timing_log="reference_timing_log.txt"

msg="Timing log: $(date)"

echo "$msg" > "$timing_log" 
echo "$msg" > "$reference_timing_log"

for d in ${d_list[@]}; do
    reference_outfile="reference_out_d${d}_t${t}.txt"
    echo -e "${CYAN}Running reference with d=${d}...${NC}"

    input="1 ${d} ${inputSuffix}"
    echo "$input" > input.txt

    # Measure the wall time of the sequential program and write it to the log
    prlimit --as=$((128 * 1024 * 1024)) time -f"t=1, d=${d}, %e s, %M kb, exit=%x" ./reference/reference < input.txt > "$reference_outfile" 2>> "$reference_timing_log"
    if [ $? -ne 0 ]; then
        echo -e "${RED}Reference failed.${NC}"
        exit 1
    fi


    for t in ${threads_list[@]}; do

        input="${t} ${d} ${inputSuffix}"
        echo "$input" > input.txt

        echo -e "${CYAN}Running parallel with d=${d} and t=${t}...${NC}"
        parallel_outfile="parallel_out_d${d}_t${t}.txt"

        threadsNumber=$((t + 1))
        
        # Measure the wall time of the parallel program and write it to the log
        prlimit --as=$((threadsNumber * 128 * 1024 * 1024)) time -f"t=${t}, d=${d}, %e s, %M kb, exit=%x" ./parallel/parallel < input.txt > "$parallel_outfile" 2>> "$timing_log"
        if [ $? -ne 0 ]; then
            echo -e "${RED}Parallel failed.${NC}"
            exit 1
        fi

        if ! diff "$reference_outfile" "$parallel_outfile"; then
            echo -e "${RED}Output files differ.${NC}"
            exit 1
        fi
    done
done

echo -e "${CYAN}Timing results are saved in ${timing_log}.${NC}"

python3 ../generate.py ${timing_log} ${reference_timing_log} "${dir}/report.pdf"
