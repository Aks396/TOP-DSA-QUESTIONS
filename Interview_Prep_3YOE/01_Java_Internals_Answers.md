# Senior Interview Answers: Java Internals & Multithreading

---

### 1. Explain the working of ConcurrentHashMap. How does it differ from a synchronized HashMap?
* **Working**: 
  * **Java 7**: Used Segmented Locking (16 segments by default). A thread lock acquired on one segment allowed other segments to be read/written concurrently.
  * **Java 8+**: Replaced segments with **CAS (Compare-And-Swap)** operations and node-level `synchronized` blocks on individual bucket heads. It uses a red-black tree (TreeBin) for collision chains exceeding a threshold of 8 elements (`TREEIFY_THRESHOLD`).
* **Differences**:
  * `synchronized HashMap` locks the entire map instance for both read and write operations.
  * `ConcurrentHashMap` allows lock-free reads and bucket-level write concurrency.
  * `ConcurrentHashMap` forbids `null` keys and values to prevent ambiguity in concurrent environments (avoiding the "Livelock" or "missing key" vs "null value" ambiguity).

---

### 2. What is the Java Memory Model (JMM)? Explain visibility and reordering.
The JMM defines how the JVM memory is structured between Thread Stacks (local variables) and the Heap (shared objects). It dictates how and when thread-local CPU cache updates are synchronized with the main memory.
* **Visibility**: CPU caches can store local copies of shared variables. Without memory barriers, threads might read stale data.
* **Instruction Reordering**: Compilers and CPUs reorder instruction execution paths to optimize pipeline execution, provided the single-threaded outcome remains unchanged. JMM restricts this around synchronization points to ensure correct multi-threaded execution.

---

### 3. Difference between `synchronized` block and `ReentrantLock`.
| Aspect | `synchronized` | `ReentrantLock` |
| :--- | :--- | :--- |
| **Management** | Implicitly by the JVM (monitor locks). | Explicit control via code (`lock()`, `unlock()`). |
| **Timeout Support** | No, blocks indefinitely. | Yes, `tryLock(timeout, unit)`. |
| **Fairness** | Unfair lock acquisition. | Optional fairness parameter (`new ReentrantLock(true)`). |
| **Interruption** | Cannot interrupt a thread waiting for it. | Yes, `lockInterruptibly()`. |
| **Conditions** | Only single monitor condition. | Multiple conditions via `lock.newCondition()`. |

*Note: Always put `unlock()` inside a `finally` block to prevent deadlock.*

---

### 4. How does Garbage Collection work in Java? Explain G1 GC vs ZGC.
* **G1 GC (Garbage First)**:
  * Designed for multi-gigabyte heaps. It divides the heap into equal-sized virtual regions.
  * It marks live objects concurrently and focuses on reclaiming regions with the most garbage first (hence "Garbage First") to minimize pause times (-XX:MaxGCPauseMillis=200).
* **ZGC (Z Garbage Collector)**:
  * A scalable, low-latency concurrent garbage collector introduced in Java 15.
  * It performs all expensive tasks (marking, relocation, remapping) concurrently with the application threads, keeping pause times under 1ms.
  * It utilizes **Colored Pointers** and **Load Barriers** to track object relocations on the fly.

---

### 5. What are the different thread states? How to handle a `BLOCKED` thread?
* **States**:
  1. `NEW`: Thread is instantiated but `start()` has not been called.
  2. `RUNNABLE`: Executing in JVM or waiting for OS processor resource.
  3. `BLOCKED`: Waiting to enter a synchronized block/method monitor lock.
  4. `WAITING`: Waiting indefinitely for another thread's signal (`Object.wait()`, `Thread.join()`).
  5. `TIMED_WAITING`: Waiting for a specific timeout period (`Thread.sleep()`, `Object.wait(timeout)`).
  6. `TERMINATED`: Thread has completed its execution.
* **Handling `BLOCKED`**: Analyze thread dumps using `jstack` or `jconsole`. Look for the monitor lock owner thread and optimize the critical section to release locks faster.

---

### 6. Explain the Fork/Join framework.
Designed for parallelizing divide-and-conquer recursive tasks (e.g., merge sort, tree traversals).
* **Fork**: Breaks a large task into smaller sub-tasks recursively until they are trivial.
* **Join**: Blocks until sub-tasks complete, then merges their results.
* **Work-Stealing Algorithm**: Each thread has its own double-ended queue (Deque) of tasks. When a thread runs out of tasks, it steals pending tasks from the *tail* of another busy thread's Deque, maximizing CPU core utilization.

---

### 7. What is a Deadlock? How can you programmatically detect or prevent it?
A deadlock occurs when Thread A holds Lock 1 and waits for Lock 2, while Thread B holds Lock 2 and waits for Lock 1.
* **Prevention**:
  * **Acquire locks in a fixed order**: Ensure all threads acquire Lock 1 before Lock 2.
  * **Use lock timeouts**: Replace synchronized blocks with `ReentrantLock.tryLock()`.
* **Programmatic Detection**:
  ```java
  ThreadMXBean bean = ManagementFactory.getThreadMXBean();
  long[] deadlockedThreads = bean.findDeadlockedThreads();
  if (deadlockedThreads != null) {
      // Trigger alerts or log stack traces
  }
  ```

---

### 8. What is AtomicInteger and how does it achieve lock-free synchronization?
`AtomicInteger` provides thread-safe operations on integer values without the overhead of locks.
* **CAS (Compare-And-Swap)**: It utilizes the CPU's native atomic instructions (like `CMPXCHG` on x86) invoked via JVM `sun.misc.Unsafe`.
* **Mechanism**:
  1. Read the current value from main memory.
  2. Perform the calculation (e.g., increment).
  3. Attempt to write the new value only if the current value in memory still matches the value read in Step 1. If it doesn't match, it spins (retries in a loop).

---

### 9. Explain the usage of `volatile` keyword. Does it provide atomicity?
* **Visibility**: Ensures changes made by one thread to a variable are written directly to main memory and immediately visible to other threads (bypassing CPU caches).
* **Instruction Ordering**: Prevents memory reordering around the variable by inserting hardware memory barriers.
* **Atomicity**: It does **NOT** provide atomicity. Operations like `count++` (which is a read-modify-write operation) are not thread-safe even if `count` is `volatile`.

---

### 10. Difference between `wait/notify` and `await/signal`?
* `wait()` / `notify()` / `notifyAll()` are inherited from `java.lang.Object` and must be called inside a `synchronized` context. They only support a single implicit wait queue per monitor.
* `await()` / `signal()` / `signalAll()` are defined in the `java.util.concurrent.locks.Condition` interface and work with `ReentrantLock`. They allow configuring multiple independent wait-sets per lock (e.g., a buffer with separate `notFull` and `notEmpty` conditions).

---

### 11. How would you implement a custom Thread Pool?
Use the core `ThreadPoolExecutor` class to customize execution parameters rather than using `Executors` factory methods (which can use unbounded queues leading to `OutOfMemoryError`).
```java
BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(100); // Bounded queue
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    10,                                  // Core pool size
    20,                                  // Maximum pool size
    60L, TimeUnit.SECONDS,               // Keep-alive time for idle threads
    queue,
    new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
);
```

---

### 12. What is ThreadLocal? Give a real-world use case.
`ThreadLocal` provides thread-confined variables. Each thread accessing a `ThreadLocal` variable gets its own independently initialized copy.
* **Use Case**: Storing user authentication context, transaction IDs, or non-thread-safe formatters (e.g., `SimpleDateFormat`) for reuse within a thread's lifecycle.
* **Memory Leak Warning**: Always call `threadLocal.remove()` in a `finally` block when running in thread pools (like Tomcat executor threads) to avoid referencing objects after the request thread is returned to the pool.

---

### 13. Explain the difference between `submit()` and `execute()` in ExecutorService.
* **`execute(Runnable)`**: Defined in the `Executor` interface. It is a "fire and forget" call. Returns `void`. If an exception occurs, it is printed to the console/logs, and the thread might terminate.
* **`submit(Runnable / Callable)`**: Defined in `ExecutorService`. It returns a `Future<?>` handle. You can call `future.get()` to retrieve results or handle checked exceptions thrown inside the task.

---

### 14. What is a Race Condition? Show a code-level fix.
A race condition happens when multiple threads access shared mutable data and attempt to update it concurrently, making the final outcome dependent on thread scheduling.
* **Unsafe Code**:
  ```java
  private int count = 0;
  public void increment() { count++; } // Read-Modify-Write (Unsafe)
  ```
* **Thread-safe Fix (using Lock)**:
  ```java
  private final ReentrantLock lock = new ReentrantLock();
  private int count = 0;
  public void increment() {
      lock.lock();
      try {
          count++;
      } finally {
          lock.unlock();
      }
  }
  ```

---

### 15. How does String internalization work in Java?
String internalization optimizes heap usage by storing only a single copy of each unique string literal in the **String Constant Pool** (residing inside the Heap).
* Literals like `String s1 = "hello"` are pooled automatically.
* Dynamic strings like `String s2 = new String("hello")` are created on the normal heap. Calling `s2.intern()` returns the reference from the pool (or adds it to the pool if not present).

---

### 16. What is the difference between `==` and `equals()`? Why override `hashCode()`?
* `==` checks for **reference equality** (whether both references point to the exact same object in memory).
* `equals()` is a method in `Object` that can be overridden to check for **value/content equality**.
* **Contract**: If `obj1.equals(obj2)` is true, their `hashCode()` values **MUST** be identical. Overriding `equals()` without `hashCode()` breaks hash-based collections (`HashMap`, `HashSet`), causing duplicate keys or lookup failures.

---

### 17. Explain String pool and the `intern()` method.
The String Pool is a JVM-managed table of string references.
* When a class is loaded, all literal strings are placed in the pool.
* Calling `String.intern()` dynamically adds a string to the pool if it isn't already present and returns its canonical reference. This saves memory for repetitive dynamically-generated strings.

---

### 18. What is the difference between `Runnable` and `Callable`?
* **`Runnable`**:
  * Functional interface with `void run()`.
  * Cannot return a result or throw checked exceptions.
* **`Callable<V>`**:
  * Functional interface with `V call()`.
  * Returns a generic result of type `V` and can throw checked exceptions.

---

### 19. What is the happens-before relationship in Java Memory Model?
A logical guarantee that memory writes by one statement are guaranteed to be visible to another statement in another thread. Key rules include:
* **Program Order Rule**: Each action in a single thread happens-before any subsequent action in that thread.
* **Monitor Lock Rule**: An unlock on a monitor happens-before every subsequent lock on that same monitor.
* **Volatile Variable Rule**: A write to a volatile field happens-before every subsequent read of that same volatile field.

---

### 20. What is the difference between `Comparable` and `Comparator`?
* **`Comparable<T>`**:
  * Defines the **natural ordering** of a class.
  * Implemented by the class itself via `public int compareTo(T o)`.
* **`Comparator<T>`**:
  * Defines **external custom orderings**.
  * Passed to sort methods (e.g., `Collections.sort(list, comparator)`) via `public int compare(T o1, T o2)`.

---

### 21. What is `finalize()` and why is it deprecated and avoided in Java 9+?
`finalize()` is called by the GC before reclaiming an object's memory. It is deprecated because:
* It has no guaranteed execution timing.
* It can cause performance degradation and GC bottlenecks.
* It can cause security vulnerabilities (object resurrection).
* *Fix*: Use `AutoCloseable` interface with try-with-resources.

---

### 22. What are functional interfaces? Give examples of core interfaces.
A functional interface is an interface containing exactly one abstract method (annotated with `@FunctionalInterface`).
* **`Predicate<T>`**: Takes `T`, returns `boolean`.
* **`Function<T, R>`**: Takes `T`, returns `R`.
* **`Consumer<T>`**: Takes `T`, returns `void`.
* **`Supplier<T>`**: Takes nothing, returns `T`.

---

### 23. How does `Stream.parallel()` work? When should it not be used?
It splits a stream's elements into segments and processes them concurrently using the shared JVM `ForkJoinPool.commonPool()`.
* **Do not use when**:
  * Operations are stateful or order-dependent (e.g., `limit()`, `findFirst()`).
  * The dataset is small (thread setup overhead exceeds execution time).
  * The operations involve blocking I/O (it can saturate the common pool and block unrelated application threads).

---

### 24. What is `Optional` and what are the best practices for using it?
A container object which may or may not contain a non-null value, designed to prevent `NullPointerException`.
* **Best Practices**:
  * Use as a **method return type** to explicitly signify potential absence.
  * Never use as a class field, method parameter, or inside collections.
  * Avoid calling `.get()` directly; use `.orElse()`, `.orElseGet()`, or `.ifPresent()`.

---

### 25. Explain static vs instance variables in concurrent environments.
* **Static variables**: Shared across all instances of a class. Concurrent modifications to static variables must be synchronized globally (e.g., locking on the `Class` object).
* **Instance variables**: Shared only among threads accessing that specific instance. Sync should lock on `this` or a private final lock object.

---

### 26. What is a memory leak in Java? Give a real-world example and how to fix it.
Occurs when unused objects are still referenced by active objects, preventing the Garbage Collector from reclaiming their memory.
* **Example**: Adding elements to a static collection (like `static Map`) indefinitely.
* **Fix**: Clean up references when no longer needed, use `WeakHashMap`, or avoid static caches.

---

### 27. What is the difference between fail-fast and fail-safe iterators?
* **Fail-Fast**:
  * Iterates directly over the collection's structure.
  * Throws `ConcurrentModificationException` if the collection is structurally modified during iteration (e.g., `ArrayList.iterator()`).
* **Fail-Safe (Weakly Consistent)**:
  * Iterates over a copy or clone of the collection.
  * Does not throw exceptions if modified during iteration (e.g., `CopyOnWriteArrayList`, `ConcurrentHashMap`).

---

### 28. Explain method references in Java 8.
A shorthand syntax for lambda expressions that call a specific method by name. Syntax uses `::`.
* **Static Method**: `Math::abs` (equivalent to `x -> Math.abs(x)`)
* **Instance Method**: `String::toLowerCase` (equivalent to `s -> s.toLowerCase()`)
* **Constructor**: `ArrayList::new` (equivalent to `() -> new ArrayList()`)

---

### 29. What is `CompletableFuture.thenCompose()` vs `thenApply()`?
* **`thenApply(Function<T, R>)`**:
  * Used for synchronous transformation (similar to `map`).
  * Returns `CompletableFuture<R>`.
* **`thenCompose(Function<T, CompletableFuture<R>>)`**:
  * Used for chaining asynchronous computations (similar to `flatMap`).
  * Flattens nested futures, returning `CompletableFuture<R>` instead of `CompletableFuture<CompletableFuture<R>>`.

---

### 30. What happens if a thread pool queue is full? Explain the policies.
If the queue is full and active threads have reached the maximum pool size, the `RejectedExecutionHandler` is triggered:
* **`AbortPolicy`** (Default): Throws `RejectedExecutionException`.
* **`CallerRunsPolicy`**: The thread that submitted the task executes it itself, slowing down submission rate.
* **`DiscardPolicy`**: Silently drops the new task.
* **`DiscardOldestPolicy`**: Discards the task at the head of the queue (oldest) and retries execution.
