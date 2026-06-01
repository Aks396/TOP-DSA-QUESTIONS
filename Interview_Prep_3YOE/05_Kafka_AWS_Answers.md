# Senior Interview Answers: Kafka & AWS Backend Engineering

---

## 🎡 A. Kafka & Event-Driven Architecture (Questions 106–115)

### 106. Explain the core architecture of Kafka.
Kafka is a distributed, horizontally scalable, crash-resilient event streaming platform.
* **Topic**: A logical channel where records are published.
* **Partition**: Topics are divided into partitions. Each partition is an ordered, immutable, append-only commit log. Partitions allow Kafka to parallelize reads and writes.
* **Offset**: A unique sequential integer assigned to each record in a partition, identifying its position.
* **Consumer Group**: A group of consumers that cooperate to consume messages from a set of partitions. Each partition in a topic is consumed by only one consumer within a group at any given time.

---

### 107. How does Kafka guarantee message ordering?
Ordering is only guaranteed **within a single partition**, not across the entire topic.
* **Mechanism**: To ensure related events (e.g., transactions for a specific user) are processed in order, use a **partition key** (e.g., `user_id`). Kafka hashes this key to route all events for that user to the same partition.
* **Producer config**: Set `max.in.flight.requests.per.connection=1` (or use `enable.idempotence=true` which internally configures this) to prevent out-of-order writes during network retries.

---

### 108. How do you scale Kafka consumers? What is the limit?
* **Scaling**: Add more consumer instances to the same consumer group. Kafka will automatically rebalance partitions across the consumers.
* **Limit**: The maximum scale limit is the **number of partitions** in the topic. If you have 10 partitions, you can have at most 10 active consumers in a group. Any additional consumers (e.g., the 11th) will remain idle.

---

### 109. What happens when a Kafka consumer is slow? How to prevent partition rebalancing?
If a consumer takes too long to process a batch of messages:
* **Outcome**: It fails to poll the broker within the configured time (`max.poll.interval.ms`). The coordinator marks it dead and triggers a **Stop-the-World partition rebalance**, pausing consumption for the entire group.
* **Prevention**:
  1. Increase `max.poll.interval.ms`.
  2. Decrease `max.poll.records` to process fewer records per poll.
  3. Offload heavy processing from the poll thread to an asynchronous worker thread pool.
  4. Use Cooperative Sticky Assignor (`CooperativeRebalanceProtocol`) to reassign only the revoked partitions, avoiding full pauses.

---

### 110. How do you handle poison pill messages in Kafka?
A poison pill is a message that repeatedly fails serialization or business validation, causing the consumer to crash or block indefinitely.
* **DLT (Dead Letter Topic) Pattern**:
  1. Wrap processing in a try-catch block.
  2. If a message fails after $N$ retry attempts, publish it to a separate topic named `<original_topic>.DLT` containing the error context.
  3. Commit the offset of the failed message to continue consuming the next messages.
  4. Have a separate background process monitor and analyze the DLT.

---

### 111. What is Kafka Log Compaction?
A mechanism that ensures Kafka retains at least the **last known value** for each key within a partition log.
* **How it works**: A background cleaner thread runs periodically to remove older duplicate keys, keeping only the latest update. Highly useful for restoring state in key-value caches or state stores (changelog topics).

---

### 112. Difference between Kafka and RabbitMQ.
| Feature | Kafka | RabbitMQ |
| :--- | :--- | :--- |
| **Type** | Distributed Commit Log. | Traditional Message Broker. |
| **Model** | Pull model (consumers request data). | Push model (broker pushes data to consumers). |
| **Message Lifetime** | Durable; stays in log based on retention. | Ephemeral; deleted after consumer ack. |
| **Routing** | Simple key-to-partition hashing. | Complex exchange types (Topic, Direct, Fanout). |
| **Throughput** | Extremely high (gigabytes/sec). | Moderate (thousands of messages/sec). |

---

### 113. How do you implement exactly-once semantics (EOS) in Kafka?
EOS ensures that messages are published exactly once by the producer and processed exactly once by the consumer, even during broker crashes or retries.
* **Configuration**:
  1. Set `enable.idempotence=true` on the producer (prevents duplicate writes).
  2. Use Kafka **Transactions API** (`beginTransaction()`, `commitTransaction()`) to write offsets and message records in a single atomic transaction.
  3. Set `isolation.level=read_committed` on the consumer to read only committed transaction data.

---

### 114. What is Kafka Streams?
An client library for building real-time, stateful/stateless stream-processing applications on top of Kafka.
* **Comparison**: Unlike standard low-level consumer APIs, Kafka Streams provides high-level DSL operations (like `map`, `filter`, `join`, `windowing`) and manages application state locally using embedded RocksDB caches, handling replication automatically.

---

### 115. Explain the three delivery guarantees in Kafka.
1. **At-most-once**: Messages may be lost but are never duplicated. (Producer setting `acks=0`; consumer commits offsets *before* processing).
2. **At-least-once**: Messages are never lost but may be duplicated. (Producer setting `acks=all` / `retries > 0`; consumer commits offsets only *after* successful processing).
3. **Exactly-once**: Messages are processed exactly once. Achieved using transactional APIs and idempotent producers.

---

## ☁️ B. AWS Backend Engineering (Questions 116–125)

### 116. Explain AWS S3 presigned URLs. When and why are they used?
A presigned URL gives temporary, restricted access to upload or download an object in a private Amazon S3 bucket.
* **Why use**: Avoids routing large files through your backend application server, saving CPU, memory, and bandwidth.
* **Flow**:
  1. Client requests a download/upload URL from the backend application.
  2. Backend validates permission and uses IAM credentials to generate a secure presigned URL with an expiry time (e.g., 15 minutes).
  3. Backend returns URL; client uploads/downloads the file directly to/from S3.

---

### 117. How do you handle AWS Lambda cold starts?
A cold start is the latency delay that occurs when a Lambda function runs for the first time or after a period of inactivity, as AWS initializes a new container container.
* **Handling strategies**:
  1. **Provisioned Concurrency**: Keeps a specified number of function instances initialized and warm.
  2. **Reduce Package Size**: Exclude unused libraries, use lightweight dependencies.
  3. **JVM Optimization**: Switch from standard Java 8/11 to GraalVM native images, or use **Lambda SnapStart** (caches container memory snapshots).
  4. **Choose Runtime**: Node.js/Python have much lower startup times than cold-JVM Java.

---

### 118. What is the difference between ALB (Application Load Balancer) and NLB (Network Load Balancer)?
* **ALB (Layer 7)**:
  * Operates at the Application layer.
  * Inspects HTTP/HTTPS headers, routing traffic based on URL path, host header, or query parameters. Supports WebSockets, gRPC, and redirect rules.
* **NLB (Layer 4)**:
  * Operates at the Transport layer (TCP, UDP, TLS).
  * Ultra-low latency, handles millions of requests per second, and uses static/Elastic IP addresses. Ideal for gaming, raw sockets, or heavy database/TCP traffic.

---

### 119. How do you make RDS highly available and scalable?
* **High Availability (HA)**: Use **Multi-AZ Deployment**. AWS maintains a synchronous hot standby database in a different Availability Zone. On primary node failure, AWS automatically fails over to the standby (typically $<30$ seconds).
* **Scalability**: Create **Read Replicas**. RDS replicates data asynchronously to replicas. Read traffic is routed to replicas while write traffic goes to the primary instance.

---

### 120. What is DynamoDB's partition key design principle? What is a hot partition?
* **Design Principle**: Choose a partition key with high cardinality (many unique values, e.g., `user_id` or `order_id`) to ensure data and request traffic are distributed evenly across physical partitions.
* **Hot Partition**: Occurs when a single partition key experiences a massive spike in requests (e.g., query partition key of a celebrity user). This can cause throttling (400 ProvisionedThroughputExceededException). Add a random numeric suffix (salting) to the partition key to split the hot load.

---

### 121. How do you secure an S3 bucket?
* Enable **Block Public Access** on the bucket level.
* Use **Bucket Policies** to restrict access to specific IAM roles or VPC endpoints.
* Enable **Default Server-Side Encryption** (SSE-S3 or SSE-KMS).
* Enforce HTTPS connection transport using bucket policy conditions (`aws:SecureTransport: false`).

---

### 122. What is AWS IAM Role vs IAM User?
* **IAM User**: A long-lived entity representing a person or application with permanent credentials (access keys, passwords). Unsafe for EC2 or Lambda use.
* **IAM Role**: An identity with temporary security credentials. Trusted entities (like EC2 instances, ECS tasks, Lambda, or external AWS accounts) can "assume" roles dynamically, eliminating the need to rotate or store secrets in application configuration files.

---

### 123. How does AWS Auto Scaling work?
* An Auto Scaling Group (ASG) manages a fleet of EC2 instances.
* It uses **Scaling Policies** triggered by CloudWatch metrics (e.g., scale out when average CPU utilization is $>70\%$ for 5 minutes, scale in when average CPU drops $<30\%$).
* **Target Tracking Policies** adjust capacity continuously to keep a specific metric close to a target value (e.g., keeping ALB requests per target at 1000).

---

### 124. What is VPC peering vs AWS Transit Gateway?
* **VPC Peering**: A direct connection between two VPCs. It is non-transitive (VPC A peering with B, and B with C does not allow A to communicate with C). It becomes hard to manage as network scale grows.
* **AWS Transit Gateway**: A network transit hub that connects thousands of VPCs and on-premises networks through a central router, resolving non-transitive peering routing tables.

---

### 125. How do you implement blue-green deployment on AWS?
A deployment strategy that reduces downtime and risk by running two identical production environments (Blue is active, Green is new).
* **Implementation**:
  1. Deploy the new application version to the Green environment (ECS/EC2).
  2. Verify Green environment correctness.
  3. Shift traffic from Blue to Green gradually or immediately using **Route 53 Weighted Routing** or **ALB Target Group shifting** (e.g. via AWS CodeDeploy).
  4. Keep Blue active for a short period to allow instant rollback if errors occur.
