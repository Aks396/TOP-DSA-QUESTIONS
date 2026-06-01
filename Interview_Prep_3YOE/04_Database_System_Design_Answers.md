# Senior Interview Answers: Database & System Design

---

### 76. Difference between SQL and NoSQL. When to use which?
* **SQL (Relational)**:
  * Strict schema, tables and relationships, structured query language, ACID compliant.
  * *Scale*: Scales vertically (bigger hardware) or via master-replica setups.
  * *Use Case*: Transactional data requiring strict consistency (e.g., core banking, billing systems).
* **NoSQL (Non-Relational)**:
  * Dynamic schema, horizontally scalable (adds more servers), BASE consistency model (Basically Available, Soft State, Eventual Consistency).
  * *Types*: Key-Value (Redis), Document (MongoDB), Wide-Column (Cassandra), Graph (Neo4j).
  * *Use Case*: Large data volume with varying shapes, high throughput low-latency reads/writes (e.g., social feeds, real-time logging, user sessions).

---

### 77. What is Database Sharding? What are the challenges?
Sharding is horizontally partitioning data across multiple independent database instances.
* **Mechanism**: A **sharding key** is used to determine which database partition holds a specific row (e.g., `user_id % num_shards`).
* **Challenges**:
  * **No Cross-Shard Joins**: Queries joining data from different shards must be joined at the application level.
  * **Distributed Transactions**: Ensuring ACID compliance across different physical databases requires 2PC/Sagas.
  * **Hotspots**: Certain shards may experience disproportionate traffic (e.g., a highly popular user's shard).
  * **Resharding Complexity**: Reshuffling data when adding new shards is operational overhead.

---

### 78. How does Caching (Redis/Memcached) improve system performance?
Caching saves frequently accessed data in fast in-memory storage (RAM), reducing access time from milliseconds to sub-milliseconds.
* **Benefits**:
  * Offloads expensive query computation from relational databases.
  * Handles massive spikes in read traffic.
* **Strategies**:
  * **Cache-Aside**: Application checks cache first. On miss, it queries the DB, writes to the cache, and returns.
  * **Write-Through**: Writes go to the cache and database simultaneously, ensuring consistency but adding write latency.
  * **Write-Behind (Write-Back)**: Writes go to cache only; database is updated asynchronously in batches. High throughput, but risk of data loss on crash.

---

### 79. Explain CAP Theorem in Distributed Systems.
In a distributed data store, you can only guarantee two out of the following three:
* **Consistency (C)**: Every read receives the most recent write or an error.
* **Availability (A)**: Every non-failing node returns a non-error response (which might contain stale data).
* **Partition Tolerance (P)**: The system continues to operate despite network partitions (dropped/delayed messages between nodes).
* **Choice**: During a network partition (P), you must choose between Consistency (CP - reject writes/reads to prevent stale data) or Availability (AP - allow writes/reads on isolated nodes, risking inconsistency).

---

### 80. What is Database Indexing? How do B-Trees work?
An index is a data structure created to speed up data retrieval operations.
* **B-Trees / B+ Trees**:
  * Self-balancing search trees designed to work efficiently on disk storage.
  * They keep data sorted and allow search, sequential access, insertion, and deletion in logarithmic time ($O(\log N)$).
  * B+ Trees store all actual records/data pointers at the leaf nodes, which are linked together in a doubly-linked list, making range queries highly efficient.
* **Cost**: Slower write/update speeds because index trees must be restructured on writes, and increased disk space usage.

---

### 81. Difference between Optimistic and Pessimistic Locking.
* **Pessimistic Locking**:
  * Assumes conflicts are highly likely. Locks the records immediately (`SELECT ... FOR UPDATE`). Other transactions block until the lock is released.
  * *Con*: Reduces concurrency and increases risk of deadlocks.
* **Optimistic Locking**:
  * Assumes conflicts are rare. No locks are acquired on read. Instead, the row has a `version` or timestamp column.
  * *Update flow*: `UPDATE table SET val = :newVal, version = version + 1 WHERE id = :id AND version = :currentVersion`.
  * *Result*: If another transaction updated the row in the meantime, the version checks fails (affected row count is 0), and the application retries. Excellent for high-concurrency systems.

---

### 82. How would you design a Rate Limiter?
* **Algorithms**:
  * **Token Bucket**: A bucket has a max capacity of tokens refilled at a constant rate. Each request consumes a token. Allows bursts.
  * **Leaky Bucket**: Requests enter a queue and leak out at a constant, smooth rate. Good for traffic shaping.
  * **Sliding Window Log**: Stores timestamps of user requests in Redis sorted set. Highly accurate, but memory-intensive.
* **Distributed Setup**: Use Redis to store counters/tokens, using Lua scripts to execute read-and-decrement actions atomically to prevent race conditions.

---

### 83. What is Content Delivery Network (CDN)?
A geographically distributed network of proxy servers that cache static assets (images, CSS, JS, HTML files) closer to the end-user (the "Edge"). It reduces origin server load and minimizes latency.

---

### 84. Explain Hashing and Consistent Hashing.
* **Simple Hashing**: Routing key to server using `hash(key) % N`.
  * *Issue*: If a server node is added or removed ($N$ changes), almost all keys are re-mapped, causing cache invalidation storms.
* **Consistent Hashing**:
  * Keys and servers are mapped to a conceptual 360-degree circle (hash ring).
  * A key is routed to the first server it encounters moving clockwise.
  * *Benefit*: When a node is added/removed, only a fraction of keys ($K / N$) need to be re-mapped.
  * *Virtual Nodes*: Virtual representations of nodes are scattered around the ring to ensure load is distributed evenly.

---

### 85. How to design a URL Shortener? (High-level design).
* **Requirements**: Take long URL -> return short URL; access short URL -> redirect to long URL.
* **Encoding**: Convert a auto-incrementing ID to Base62 (characters `A-Z`, `a-z`, `0-9`). A 7-character string allows $62^7 \approx 3.5$ trillion unique short URLs.
* **Architecture**:
  * Write path: Client -> API Gateway -> Write Service -> Generates unique Base62 hash -> saves in NoSQL DB (MongoDB/Cassandra) -> writes to Redis.
  * Read path: Client -> API Gateway -> Read Service -> Checks Redis cache. If hit, issues `302 Found` redirect. If miss, fetches from DB, updates Redis, and redirects.

---

### 86. What is the difference between `WHERE` and `HAVING` in SQL?
* **`WHERE`**: Filters records *before* any grouping (GROUP BY) takes place. Cannot contain aggregate functions (e.g., `SUM()`, `COUNT()`).
* **`HAVING`**: Filters groups *after* grouping has been performed. Used to filter based on aggregate conditions (e.g., `HAVING COUNT(*) > 5`).

---

### 87. Difference between `INNER`, `LEFT`, `RIGHT`, and `FULL JOIN`?
* **`INNER JOIN`**: Returns rows only when there is a match in both tables.
* **`LEFT JOIN`**: Returns all rows from the left table, and matched rows from the right table. (Unmatched right table columns are `NULL`).
* **`RIGHT JOIN`**: Returns all rows from the right table, and matched rows from the left table.
* **`FULL JOIN`**: Returns rows when there is a match in either the left or right table.

---

### 88. What is a covering index in databases?
A covering index is a composite index that contains all the columns requested in the query's select list, join keys, and filter clauses.
* *Benefit*: The database engine can satisfy the query entirely from the index data structure without performing a costly disk lookup on the actual table heap (known as "Index Only Scan").

---

### 89. How do you find duplicate rows in a table?
Use `GROUP BY` and `HAVING`:
```sql
SELECT email, COUNT(*) 
FROM users 
GROUP BY email 
HAVING COUNT(*) > 1;
```

---

### 90. What is a database deadlock and how do you detect/resolve it?
* **Deadlock**: Two transactions wait indefinitely for locks held by each other.
* **Detection**: Database engines use a dependency graph (Wait-For Graph) to detect cycles.
* **Resolution**: The database automatically kills/rolls back one of the transactions (the one with the lowest cost/impact) and returns an error to the application, allowing it to retry.

---

### 91. Explain MVCC (Multi-Version Concurrency Control).
MVCC is a concurrency control method used by modern databases (PostgreSQL, MySQL InnoDB) to allow lock-free concurrent reads and writes.
* **Mechanism**: Instead of locking rows on write, the database maintains multiple historical versions of each modified row.
* **Read-Consistency**: A read operation sees a consistent snapshot of the database at the start of the transaction, meaning readers do not block writers, and writers do not block readers.

---

### 92. What is a window function in SQL? Give an example.
A window function performs a calculation across a set of table rows that are related to the current row, without collapsing the rows into a single output group.
```sql
SELECT employee_name, department, salary,
       RANK() OVER (PARTITION BY department ORDER BY salary DESC) as salary_rank
FROM employees;
```

---

### 93. What is the database N+1 query problem and how to resolve it in Hibernate?
* **Problem**: Occurs when loading a parent entity with $N$ lazy-loaded child relationships. Hibernate runs 1 query to fetch the parent rows, and then executes $N$ separate queries to fetch the children of each parent (e.g. 100 orders -> 101 queries).
* **Fixes**:
  * **JOIN FETCH**: Force eager loading via a join in JPQL: `SELECT o FROM Order o JOIN FETCH o.items`.
  * **EntityGraph**: Annotate or dynamically specify fetch graphs.
  * **BatchSize**: Annotate the collection with `@BatchSize(size = 20)` to fetch child entities in batches rather than individually.

---

### 94. What is table partitioning?
Splitting a single logical table into multiple smaller physical tables (partitions) based on a partition column (e.g., partitioning a `logs` table by range of `created_at` date).
* *Benefit*: **Partition Pruning** allows the database planner to skip scanning partitions that fall outside the query's date filter, drastically improving scan speed.

---

### 95. How do you analyze and optimize a slow query?
1. Execute **`EXPLAIN ANALYZE`** on the query to view the database execution plan, costs, and actual timings.
2. Look for **Seq Scan** (Sequential Scan) on large tables and add appropriate indexes on filter/join columns.
3. Optimize queries by avoiding wildcard selects (`SELECT *`) and indexing fields in order of cardinality.
4. Avoid executing database functions on index columns (e.g., `WHERE YEAR(created_at) = 2024` kills index usage; rewrite as `WHERE created_at >= '2024-01-01'`).

---

### 96. What is the difference between `TRUNCATE` and `DELETE`?
* **`DELETE`**: A DML (Data Manipulation Language) command. It deletes rows one-by-one, logs each deletion in the transaction log, and fires triggers. Can have a `WHERE` clause.
* **`TRUNCATE`**: A DDL (Data Definition Language) command. It deallocates the data pages directly. It is much faster, cannot have a `WHERE` clause, resets identity columns, and does not fire delete triggers.

---

### 97. What is a composite key vs a surrogate key?
* **Composite Key**: A primary key consisting of two or more columns that uniquely identify a row (e.g., `order_id`, `item_id`).
* **Surrogate Key**: An artificial, system-generated primary key (like a UUID or auto-incrementing integer) that has no business meaning but is guaranteed to be unique and stable.

---

### 98. What is connection pooling? How do you calculate pool size?
* **Pooling**: Reusing pre-established database connections instead of opening and closing physical connections for every query, which is computationally expensive.
* **Pool Size calculation**: Too many connections cause disk thrashing and CPU context-switching. The classic HikariCP formula is:
  $$\text{Connections} = (\text{Core Count} \times 2) + \text{Disk Spindle Count}$$

---

### 99. Design a Twitter/X Feed (Fan-out on write vs read).
* **Requirements**: Post tweets, follow users, generate timeline.
* **Fan-out on Read (Pull)**:
  * When a user loads their home timeline, fetch their follows list, fetch tweets from all followed users, sort by time, and render.
  * *Con*: Very slow on read for users following thousands of accounts.
* **Fan-out on Write (Push)**:
  * When a user posts a tweet, find all their followers, and insert the tweet ID directly into each follower's pre-computed inbox cache (in Redis).
  * *Con*: Slow on write for celebrity users (millions of followers).
* **Hybrid approach**: Use Push (Fan-out on write) for normal users, and pull (Fan-out on read) for celebrity/influencer tweets, merging them in the application layer during read.

---

### 100. Design WhatsApp/Chat System (WebSockets, message ordering).
* **Architecture**: Clients connect to Chat Servers via **WebSockets** (persistent bi-directional TCP connections).
* **Message Delivery**:
  1. Client A sends message to WebSocket Server.
  2. Server saves message to database (Cassandra/DynamoDB for high write scale).
  3. Server checks if Client B is connected. If online, pushes message immediately.
  4. If offline, pushes a push notification (FCM/APNS).
* **Ordering**: Use a sequence-generator or epoch timestamp combined with logical clocks (vector clocks) to ensure messages render in order at client side.

---

### 101. Design Uber/Ola (Geohashing, driver matching).
* **Architecture**: High write volume of driver coordinate updates.
* **Spatial Indexing**: Use **Geohashing** or **Google S2 geometry** to divide the map into hierarchical grid cells.
* **Matching**:
  * Drivers update coordinates every 4 seconds. Updates go to a fast cache (Redis geospatial index: `GEOADD`).
  * When a rider requests a ride, the system converts rider's location to a geohash, queries Redis for nearby driver geohashes, calculates distances, and sends ride requests.

---

### 102. Design Netflix/YouTube (CDN, adaptive bitrate streaming).
* **Upload Path**: Video uploaded -> split into chunks -> Transcoding engine encodes video chunks into multiple resolutions (1080p, 720p, 480p) and formats (HLS, DASH).
* **Delivery Path**: Chunks are uploaded to Amazon S3 and distributed to global edge servers (CDNs) located close to users.
* **Adaptive Streaming**: The client player adjusts video quality dynamically based on network bandwidth by requesting appropriate resolution chunks.

---

### 103. Design a Payment System (Idempotency, saga pattern, compliance).
* **Core Requirements**: Never double-charge; high availability and auditability.
* **Idempotency Key**: Client sends a unique UUID `Idempotency-Key` in headers. The API Gateway stores this key along with the status in Redis. If a retry occurs with the same key, it returns the cached response rather than processing the transaction again.
* **Architecture**: Uses the Saga pattern to reserve funds, execute charging with the bank processor, update ledger databases, and finalize order states.

---

### 104. Design a Distributed ID Generator (Snowflake ID).
To generate globally unique, chronological 64-bit integers without a centralized coordinator:
* **Bit Allocation (Twitter Snowflake)**:
  * **1 bit**: Unused sign bit.
  * **41 bits**: Epoch Millisecond timestamp (allows $\sim 69$ years of usage).
  * **10 bits**: Machine ID / Worker ID (supports 1024 unique server nodes).
  * **12 bits**: Sequence number (supports 4096 IDs per millisecond per machine).

---

### 105. Design a Distributed Cache (Consistent hashing, eviction).
* **Structure**: Master-replica caching nodes.
* **Routing**: Consistent hashing ring determines which cache node stores which key.
* **Eviction Policies**: LRU (Least Recently Used), LFU (Least Frequently Used), TTL expiry.
* **Consistency**: Use Write-through or Redis Pub/Sub to invalidate cache entries upon main DB writes.
