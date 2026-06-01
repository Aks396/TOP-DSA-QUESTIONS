# Senior Interview Answers: Spring Boot & Microservices

---

### 46. What is the Saga Pattern? How do you manage distributed transactions?
In a microservices architecture, services have separate databases, making traditional 2-Phase Commit (2PC) slow and prone to blocking. The **Saga Pattern** manages transactions via a sequence of local transactions.
* **Mechanism**: Each service performs its local transaction and publishes an event or message to trigger the next service's local transaction.
* **Compensating Transactions**: If any step fails, the Saga orchestrator or participants execute rollback/undo operations (compensating transactions) in reverse order to achieve eventual consistency.
* **Types**:
  * **Choreography**: Event-driven; participants react to events from other services without a central coordinator.
  * **Orchestration**: A central orchestrator service tells participants what local transactions to execute.

---

### 47. Difference between Monolithic and Microservices architecture.
* **Monolith**:
  * *Pros*: Simple to build, test, and deploy initially; low network latency (in-memory calls).
  * *Cons*: Single point of failure; hard to scale individual modules; code base complexity grows; technology stack lock-in.
* **Microservices**:
  * *Pros*: Independent deployment and scaling; technology diversity; isolated fault domains; smaller, focused teams.
  * *Cons*: Network latency and network partition challenges; data consistency issues (eventual consistency); operational and deployment complexity (K8s, monitoring).

---

### 48. What is a Circuit Breaker? How does Resilience4j work?
A circuit breaker prevents cascading failures by stopping calls to a failing downstream service.
* **States**:
  * **Closed**: Normal state; requests flow to the downstream service. If failure rate exceeds a threshold, it transitions to Open.
  * **Open**: Requests fail fast immediately, invoking a fallback method without hitting the downstream service.
  * **Half-Open**: After a cooldown period, it allows a limited number of test requests. If they succeed, it closes; if they fail, it opens again.
* **Resilience4j**: A lightweight fault tolerance library for Java. Can be configured using annotations: `@CircuitBreaker(name = "backendService", fallbackMethod = "fallbackPay")`.

---

### 49. Explain Service Discovery (Eureka/Consul).
In dynamic environments, service instances scale up/down, meaning IP addresses and ports change dynamically.
* **Mechanism**:
  1. **Registry**: A central registry (like Netflix Eureka Server) maintains active service instance addresses.
  2. **Registration**: When a service starts, it registers its IP and port with the Eureka server.
  3. **Discovery**: When Service A wants to call Service B, it queries Eureka for B's instances and load-balances the call (e.g., using Spring Cloud LoadBalancer).

---

### 50. What is an API Gateway? What are its core responsibilities?
A single entry point for all clients to route requests to backend microservices.
* **Responsibilities**:
  * **Routing**: Directing client requests to appropriate services.
  * **Authentication/Authorization**: Verifying tokens (JWT) before forwarding.
  * **Rate Limiting**: Throttling malicious or high-traffic clients.
  * **Cross-Cutting Concerns**: SSL termination, logging, and metrics aggregation.

---

### 51. Explain Event-Driven Architecture. When to use Kafka vs RabbitMQ?
* **Event-Driven**: Systems communicate asynchronously by publishing and consuming events, decoupling producers from consumers.
* **RabbitMQ**: A traditional message broker using the AMQP protocol. It pushes messages to consumers and deletes them after acknowledgment. Best for complex routing keys and transactional message flows.
* **Kafka**: A distributed append-only commit log. Consumers pull messages. Messages are durable and replayable. Best for high throughput streams, event replays, log aggregation, and real-time processing.

---

### 52. How to handle data consistency across microservices?
* Avoid distributed transactions (2PC/XA) due to performance overhead.
* Implement **Eventual Consistency** using message queues.
* Implement the **Outbox Pattern** to ensure database updates and event publishing occur atomically.
* Use idempotent consumers to handle duplicate messages.

---

### 53. What is CQRS? When is it used?
* **CQRS (Command Query Responsibility Segregation)**: Splits the application into a **Write side** (Commands: insert/update/delete) and a **Read side** (Queries: fetch data).
* **When to use**: High-performance systems where read load differs drastically from write load. For instance, writes go to a transactional PostgreSQL database, which publishes events to sync an Elasticsearch index optimized for complex, fast reads.

---

### 54. Explain the concept of Backpressure in Reactive Programming.
Backpressure is a flow-control feedback loop where a slow consumer signals to a fast producer to slow down the data emission rate, preventing the consumer's memory buffer from overflowing (used in Spring WebFlux, Project Reactor).

---

### 55. How do you secure a Microservice? (OAuth2/JWT).
* **JWT (JSON Web Token)** is used for stateless authentication. It contains three parts: Header, Payload (claims like user details), and Signature (signed using HMAC-SHA256 or RSA-256).
* **Flow**:
  1. Client logs in via Identity Provider (OAuth2 Authorization Server).
  2. Client receives a JWT signed with the Auth Server's private key.
  3. Client passes this JWT in the `Authorization: Bearer <token>` header for subsequent microservice requests.
  4. Microservices validate the signature using the Auth Server's public key without needing a network call back to the Auth Server.

---

### 56. What is Centralized Logging? (ELK/Splunk).
Centralized logging aggregates log streams from all distributed microservice instances into a single searchable store.
* **ELK Stack**: **Logstash** (or Filebeat) collects and parses logs, **Elasticsearch** indexes them for search, and **Kibana** provides a dashboard for log visualization.
* **Tip**: Include a unique `correlationId` (MDC context) in every request header and propagate it across all service calls to trace logs for a single request end-to-end.

---

### 57. Difference between Spring Boot @Component, @Service, and @Repository.
All three are stereotype annotations that mark classes for Spring bean discovery.
* **`@Component`**: The generic archetype.
* **`@Service`**: Used at the business service layer.
* **`@Repository`**: Used at the DAO/Data access layer. It enables automatic **PersistenceExceptionTranslationPostProcessor** which translates database-specific SQL exceptions into Spring's unified `DataAccessException` hierarchy.

---

### 58. What is Bean Lifecycle in Spring? Explain the exact sequence.
Spring Beans go through a strict lifecycle managed by the ApplicationContext:
1. **Instantiation**: Spring instantiates the bean instance using constructor or factory method.
2. **Populate Properties**: Dependencies are injected via setter or field injection.
3. **Aware Interfaces**: If the bean implements `BeanNameAware`, `BeanFactoryAware`, or `ApplicationContextAware`, Spring calls their setter methods.
4. **BeanPostProcessor (Before Initialization)**: `postProcessBeforeInitialization()` is called on all post-processors (e.g. processing `@PostConstruct`).
5. **Initialization**: Calls custom init methods (implementing `InitializingBean` or defined in `@Bean(initMethod)`).
6. **BeanPostProcessor (After Initialization)**: `postProcessAfterInitialization()` is called (this is where **AOP proxy wrappers** are created).
7. **Bean is Ready**: Bean is active and available in the context.
8. **Destruction**: When the context closes, it calls methods annotated with `@PreDestroy` or custom destroy methods.

---

### 59. Explain the working of @Transactional. What are propagation and isolation levels?
* **Working**: Spring wraps the class in an AOP proxy. When a transactional method is called, the proxy starts a JDBC transaction. If the method completes successfully, it commits; if a `RuntimeException` is thrown, it rolls back.
* **Propagation Levels**:
  * `REQUIRED` (Default): Joins the active transaction or creates a new one if none exists.
  * `REQUIRES_NEW`: Always suspends the current transaction and starts a new independent transaction.
  * `MANDATORY`: Requires an existing active transaction; otherwise throws an exception.
* **Isolation Levels**:
  * `READ_UNCOMMITTED`, `READ_COMMITTED` (PG default), `REPEATABLE_READ` (MySQL default), `SERIALIZABLE`.

---

### 60. How to monitor microservices? (Prometheus/Grafana).
* **Spring Boot Actuator**: Exposes metrics endpoints like `/actuator/prometheus`.
* **Prometheus**: A time-series database that scrapes metrics from the actuator endpoint at configured intervals.
* **Grafana**: Pulls data from Prometheus and visualizes it on rich dashboards (CPU load, request latency, JVM heap usage, HTTP error counts).

---

### 61. How does `@Autowired` work internally?
It is processed by the **`AutowiredAnnotationBeanPostProcessor`**. During bean initialization, Spring scans fields, constructors, and setter methods annotated with `@Autowired`, finds a matching bean by **Type** in the context, and injects it. If multiple beans match, it uses **`@Qualifier`** or falls back to name-matching.

---

### 62. What is the difference between `@Bean` and `@Component`?
* **`@Component`**: Class-level annotation used for auto-detection and classpath scanning.
* **`@Bean`**: Method-level annotation used inside `@Configuration` classes. It is used to register third-party library classes (which you cannot annotate with `@Component`) as beans.

---

### 63. How does Spring Boot handle circular dependencies?
* **Constructor Injection**: Fails immediately at startup, throwing `BeanCurrentlyInCreationException`.
* **Setter / Field Injection**: Spring uses a **three-stage cache** (`singletonObjects`, `earlySingletonObjects`, and `singletonFactories`) inside `DefaultSingletonBeanRegistry`. It exposes a partially constructed, uninitialized bean reference early, allowing circular references to resolve.
* *Fix*: Refactor code to remove circularity or annotate a field with `@Lazy`.

---

### 64. What is @Transactional self-invocation issue and how to resolve it?
* **Issue**: If Method A calls Method B (annotated with `@Transactional`) within the same class, the transaction is **bypassed**. This occurs because the internal call bypasses the AOP proxy wrapper and invokes the method directly on `this`.
* **Fix**:
  1. Move the transactional method to a separate helper class.
  2. Inject the proxy of the class into itself using `@Autowired` or `@Lazy`.
  3. Use `AopContext.currentProxy()` (requires enabling exposeProxy).

---

### 65. Explain `@Cacheable`, `@CachePut`, and `@CacheEvict`.
* **`@Cacheable`**: Checks the cache first. If found, returns the cached value; otherwise, executes the method and caches the result.
* **`@CachePut`**: Always executes the method and updates the cache with the new result.
* **`@CacheEvict`**: Removes entries from the cache (e.g., when a record is deleted).

---

### 66. What is Spring Boot Auto-configuration?
It attempts to automatically configure your Spring application based on the jar dependencies present on the classpath.
* **Mechanism**: Handled by **`@EnableAutoConfiguration`** (part of `@SpringBootApplication`). It reads key-value pairs from `META-INF/spring.factories` and evaluates conditional annotations like `@ConditionalOnClass` and `@ConditionalOnMissingBean` to instantiate configuration classes.

---

### 67. How do you implement rate limiting in Spring Boot?
* **Gateway level**: Use Spring Cloud Gateway filters with Redis Rate Limiter (Token Bucket algorithm).
* **Application level**: Use a servlet filter or interceptor with libraries like **Bucket4j** or resilience libraries.

---

### 68. What is the difference between `@RestController` and `@Controller`?
* **`@Controller`**: Standard MVC controller used to serve web pages (views).
* **`@RestController`**: A convenience annotation that combines `@Controller` and `@ResponseBody`. It automatically serializes the returned object into JSON or XML response bodies.

---

### 69. How does global exception handling work in Spring Boot?
Annotate a class with **`@ControllerAdvice`** or **`@RestControllerAdvice`** and define methods annotated with **`@ExceptionHandler(Exception.class)`**. This interceptor catches all controller exceptions globally and returns a unified client error body (e.g., using the RFC 7807 Problem Details standard).

---

### 70. What is the difference between EAGER and LAZY loading?
* **EAGER**: Loads the associated child relationships immediately when loading the parent entity.
* **LAZY**: Loads the associated relationships dynamically only when they are accessed (e.g., `parent.getChildren()`).
* *Risk*: LAZY loading can throw a `LazyInitializationException` if the Hibernate session is closed, or trigger `N+1` select queries if not fetched properly.

---

### 71. How do you implement async processing in Spring Boot using `@Async`?
1. Annotate a configuration class with **`@EnableAsync`**.
2. Annotate a method with **`@Async`**. The method must return `void` or a `CompletableFuture`.
3. Customize a `ThreadPoolTaskExecutor` bean to handle thread scheduling.

---

### 72. How do you handle database migrations in microservices? (Flyway/Liquibase).
* Store SQL migration scripts inside `src/main/resources/db/migration`.
* At application startup, Flyway compares the scripts with its metadata table (`schema_version`) and runs new scripts in transactional order, ensuring zero-downtime database structure deployments.

---

### 73. What is the Outbox Pattern? Why is it used?
Ensures database updates and event publishing to a message broker (like Kafka) happen atomically to prevent data loss or inconsistency.
* **Mechanism**: Instead of publishing to Kafka directly during a REST request, the service writes the event to a database table named `outbox` within the *same* database transaction. A separate CDC tool (like Debezium) or poll scheduler reads the outbox table and publishes events to Kafka.

---

### 74. How do you secure inter-service communication? (mTLS vs API Keys).
* **mTLS (Mutual TLS)**: Both client and server authenticate each other’s certificates. Highly secure, usually managed at the service mesh layer (e.g., Istio).
* **API Keys / JWT Propagation**: Services pass signed internal service-to-service JWT tokens in headers.

---

### 75. What is the Bulkhead pattern in resilience?
Isolates resources (like thread pools or connection pools) so that a failure in one service or operation does not consume all system resources and bring down the entire application.
