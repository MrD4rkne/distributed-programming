import argparse
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages
import re
from math import ceil
import matplotlib.cm as cm

# Function to parse the timing log file
def parse_log(filename):
    data = []
    with open(filename, 'r') as file:
        for line in file:
            match = re.match(r't=(\d+), d=(\d+), (\d+\.\d+) s, (\d+) kb, exit=0', line)
            if match:
                t, d, time, memory = match.groups()
                data.append({
                    't': int(t),
                    'd': int(d),
                    'time': float(time),
                    'memory': int(memory)
                })

                if data[-1]['time'] > 60:
                    data[-1]['time'] = -1  # Mark long times as invalid
                # for now, we do not allow 0 time
                if data[-1]['time'] == 0:
                    data[-1]['time'] = 0.01
    return pd.DataFrame(data)

# Function to calculate scalability rate for each thread count
def calculate_scalability_rate(df, single_thread_df):
    scalability_data = []
    
    for d in df['d'].unique():
        # Get the single-thread time from the other file
        single_thread_time = single_thread_df[single_thread_df['d'] == d]['time'].values[0]

        # Calculate scalability for each thread count (t)
        for t in df[df['d'] == d]['t'].unique():
            multi_thread_time = df[(df['d'] == d) & (df['t'] == t)]['time'].values[0]
            scalability_rate = single_thread_time / multi_thread_time if multi_thread_time > 0 else float('inf')
            scalability_data.append({
                'd': d,
                't': t,
                'time': multi_thread_time,
                'scalability_rate': scalability_rate
            })
    return pd.DataFrame(scalability_data)

# Function to generate PDF report
def generate_pdf(df, scalability_df, single_thread_df, output_pdf):
    def chunk_dataframe(dataframe, rows_per_page):
        """Split the DataFrame into chunks of `rows_per_page` rows."""
        total_rows = len(dataframe)
        num_chunks = ceil(total_rows / rows_per_page)
        return [dataframe.iloc[i * rows_per_page:(i + 1) * rows_per_page] for i in range(num_chunks)]

    rows_per_page = 30  # Adjust this value to fit the table on a page

    with PdfPages(output_pdf) as pdf:
        # Title page
        fig, ax = plt.subplots(figsize=(8, 6))
        ax.text(0.5, 0.8, 'Scalability Report', ha='center', fontsize=16, fontweight='bold')
        ax.text(0.5, 0.7, 'Large Assignment (C)', ha='center', fontsize=14)
        ax.text(0.5, 0.6, 'Author: Marcin Szopa', ha='center', fontsize=12)
        ax.text(0.5, 0.5, 'Index: 459531', ha='center', fontsize=12)
        ax.axis('off')
        pdf.savefig(fig)
        plt.close()

        # Navigation page
        fig, ax = plt.subplots(figsize=(8, 6))
        ax.text(0.5, 0.8, 'Structure', ha='center', fontsize=14, fontweight='bold')
        ax.text(0.5, 0.7, '1. Scalability Rate by thread count for each dataset size', ha='center', fontsize=12)
        ax.text(0.5, 0.6, '2. Combined Scalability Rate by thread count (all dataset sizes)', ha='center', fontsize=12)
        ax.text(0.5, 0.5, '3. Timing by thread count for each dataset size', ha='center', fontsize=12)
        ax.text(0.5, 0.4, '4. Parallel Data Table', ha='center', fontsize=12)
        ax.text(0.5, 0.3, '5. Reference Data Table', ha='center', fontsize=12)
        ax.axis('off')
        pdf.savefig(fig)
        plt.close()

        # Generate individual plots for each dataset size (d)
        cmap = cm.get_cmap('tab10')  # Get a color map to assign different colors
        for i, d in enumerate(df['d'].unique()):
            d_scalability_df = scalability_df[scalability_df['d'] == d]
            
            # Plot scalability rate for each dataset size (d)
            plt.figure(figsize=(8, 6))
            plt.plot(d_scalability_df['t'], d_scalability_df['scalability_rate'], marker='o', color=cmap(i), label=f'd={d} Scalability Rate')
            plt.plot(d_scalability_df['t'], d_scalability_df['t'], 'r--', label='y = x')
            plt.title(f'Scalability Rate by thread count for d={d}')
            plt.xlabel('t (Thread Count)')
            plt.ylabel('Scalability Rate')
            plt.grid(True)
            plt.legend()
            pdf.savefig()
            plt.close()

        # Generate a combined plot with all dataset sizes
        plt.figure(figsize=(8, 6))
        for i, d in enumerate(df['d'].unique()):
            d_scalability_df = scalability_df[scalability_df['d'] == d]
            plt.plot(d_scalability_df['t'], d_scalability_df['scalability_rate'], marker='o', color=cmap(i), label=f'd={d}')
        plt.plot(d_scalability_df['t'], d_scalability_df['t'], 'r--', label='y = x')
        plt.title('Scalability Rate by thread count (all d values)')
        plt.xlabel('t (Thread Count)')
        plt.ylabel('Scalability Rate')
        plt.grid(True)
        plt.legend()
        pdf.savefig()
        plt.close()

        # Generate timing plots for each dataset size (d) in normal scale
        for i, d in enumerate(df['d'].unique()):
            d_df = df[df['d'] == d]
            
            # Plot timing for each dataset size (d)
            plt.figure(figsize=(8, 6))
            plt.plot(d_df['t'], d_df['time'], marker='o', color=cmap(i), label=f'd={d} Time')
            plt.title(f'Time by thread count for d={d}')
            plt.xlabel('t (Thread Count)')
            plt.ylabel('Time (s)')
            plt.grid(True)
            plt.legend()
            pdf.savefig()
            plt.close()

        # Table of scalability data
        fig, ax = plt.subplots(figsize=(8, 6))
        ax.text(0.5, 0.8, 'Scalability Data Table', ha='center', fontsize=14, fontweight='bold')
        ax.axis('off')
        pdf.savefig(fig)
        plt.close()

        table_chunks = chunk_dataframe(scalability_df, rows_per_page)
        for chunk in table_chunks:
            fig, ax = plt.subplots(figsize=(8, 6))
            ax.axis('tight')
            ax.axis('off')
            ax.table(cellText=chunk.values, colLabels=chunk.columns, loc='center', cellLoc='center')
            pdf.savefig(fig)
            plt.close()

        # Table of single-thread data
        fig, ax = plt.subplots(figsize=(8, 6))
        ax.text(0.5, 0.8, 'Reference Data Table', ha='center', fontsize=14, fontweight='bold')
        ax.axis('off')
        pdf.savefig(fig)
        plt.close()

        single_thread_chunks = chunk_dataframe(single_thread_df, rows_per_page)
        for chunk in single_thread_chunks:
            fig, ax = plt.subplots(figsize=(8, 6))
            ax.axis('tight')
            ax.axis('off')
            ax.table(cellText=chunk.values, colLabels=chunk.columns, loc='center', cellLoc='center')
            pdf.savefig(fig)
            plt.close()

# Main function
def main():
    parser = argparse.ArgumentParser(description='Process timing log and generate PDF report.')
    parser.add_argument('log_filename', type=str, help='The timing log file')
    parser.add_argument('single_thread_filename', type=str, help='The file containing single-thread times')
    parser.add_argument('output_pdf', type=str, help='Output PDF file name')
    args = parser.parse_args()

    # Parse the log file and the single-thread time file
    df = parse_log(args.log_filename)

    print(df)

    single_thread_df = parse_log(args.single_thread_filename)

    print(single_thread_df)

    # Calculate scalability rate for each thread count
    scalability_df = calculate_scalability_rate(df, single_thread_df)

    print(scalability_df)

    # Generate PDF report
    generate_pdf(df, scalability_df, single_thread_df, args.output_pdf)
    print(f"PDF report generated: {args.output_pdf}")

if __name__ == '__main__':
    main()
