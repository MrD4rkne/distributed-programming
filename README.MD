# Distributed Programming

## Projects

### Project 1: Boolean Formula Trees

Implementation of a multithreaded system to efficiently compute trees of boolean formulas, leveraging parallel processing to optimize evaluation performance.

### Project 2: High-Performance Computation

### Project 2: High-Performance Computation

Implementation of an optimized multithreaded application for processing large volumes of computational tasks. The solution employs a batch-checking strategy where threads access the shared task queue only every `M` iterations and in almost lock-free way, significantly reducing synchronization overhead and improving throughput.

The application is configurable with command-line arguments for thread count and problem parameters.

Performance results are available on [Marcin Wrochna's page](https://mimuw.edu.pl/~mwrochna/upload/pw2425hw2/ms459531_84f5909f.html), showing comparative analysis of throughput and efficiency metrics.

Performance benchmarks **demonstrated execution speeds surpassing reference implementations**, including those developed by course instructors. That's what I'm proud of.

#### Performance of my solution

[Performance of my solution](./zal2/docs/pw_hw2.png)

#### Performance of reference solution

[Performance of reference solution](./zal2/docs/pw_hw2_perfect.png)

## Theoritical

### Multithreading

Explore mutexes and blocking queues. Understand common pitfalls such as deadlocks and thread starvation, and learn strategies to avoid blocking your code.

### Synchronous vs. Asynchronous Communication

Learn abstract coding techniques to prevent your applications from becoming unresponsive, focusing on best practices for both synchronous and asynchronous communication between hosts.