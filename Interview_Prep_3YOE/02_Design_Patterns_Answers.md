# Senior Interview Answers: Design Patterns & SOLID

---

### 31. Explain the SOLID principles with real-world examples.
* **S - Single Responsibility Principle (SRP)**:
  * *Definition*: A class should have only one reason to change.
  * *Example*: Instead of a single `Invoice` class that calculates totals, saves to a DB, and sends email notifications, split it into three classes: `InvoiceCalculator`, `InvoiceRepository`, and `NotificationService`.
* **O - Open/Closed Principle (OCP)**:
  * *Definition*: Software entities should be open for extension but closed for modification.
  * *Example*: Create an interface `PaymentProcessor`. If you need to support a new payment method (like Stripe), create a new class implementing the interface rather than modifying the existing `PaymentProcessor` code.
* **L - Liskov Substitution Principle (LSP)**:
  * *Definition*: Subclasses should be substitutable for their superclasses without breaking the system.
  * *Example*: A `Ostrich` class inheriting from `Bird` but throwing an exception in `fly()` violates LSP. Fix: split behaviors into separate interfaces like `Flyable` and `Walkable`.
* **I - Interface Segregation Principle (ISP)**:
  * *Definition*: Clients should not be forced to depend on methods they do not use (prefer small, specialized interfaces).
  * *Example*: Instead of a large `Worker` interface with `work()` and `eat()`, split it into `Workable` and `Eatable`.
* **D - Dependency Inversion Principle (DIP)**:
  * *Definition*: Depend on abstractions, not concretions.
  * *Example*: A `Car` class should depend on an `Engine` interface rather than a concrete `V8Engine` class, allowing easy engine swapping.

---

### 32. Difference between Factory and Abstract Factory patterns.
* **Factory Pattern**:
  * Creates objects of a single family/hierarchy (e.g., `ShapeFactory` returns `Circle` or `Rectangle` based on an input string).
  * Consists of a single creator method.
* **Abstract Factory Pattern**:
  * A "Factory of Factories". It creates families of related or dependent objects (e.g., `GUIFactory` creates both `Button` and `Checkbox` compatible with a specific OS: `MacFactory` returns `MacButton` + `MacCheckbox`, while `WinFactory` returns `WinButton` + `WinCheckbox`).
  * Consists of multiple creator methods.

---

### 33. When would you use the Prototype pattern over the Factory pattern?
Use the **Prototype pattern** when:
* Creating a new instance is computationally expensive (e.g., loading configurations or complex state databases).
* You want to clone an object's existing state rather than initializing it from scratch.
* *How it works*: Implementing `Cloneable` and overriding `clone()` to perform a deep copy.

---

### 34. Explain the Proxy Pattern. How is it used in Spring AOP?
The Proxy pattern provides a placeholder/surrogate object to control access to a target object.
* **Spring AOP**:
  * Uses **JDK Dynamic Proxies** (if the target class implements an interface) or **CGLIB** (subclassing bytecode generation if it does not) to create a proxy wrapper.
  * When you invoke a method annotated with `@Transactional`, the call is intercepted by the proxy, which initiates the database transaction, runs your method, and then commits or rolls back the transaction.

---

### 35. What is the Strategy pattern?
Defines a family of interchangeable algorithms and encapsulates each one, decoupling them from the client using them.
```java
public interface PaymentStrategy {
    void pay(int amount);
}
public class CreditCardPayment implements PaymentStrategy {
    public void pay(int amount) { System.out.println("Paid " + amount + " via Card"); }
}
public class PaypalPayment implements PaymentStrategy {
    public void pay(int amount) { System.out.println("Paid " + amount + " via PayPal"); }
}
```

---

### 36. Explain the Observer pattern. How does Java's Event model use it?
Defines a one-to-many dependency where one state change in the subject automatically notifies and updates all registered observers.
* **Usage**: Spring’s `ApplicationEventPublisher` and listeners (`@EventListener`) implement this pattern to handle decoupled system events.

---

### 37. What is the Decorator pattern? How does Java I/O (`BufferedReader`) use it?
Dynamically attaches new responsibilities to an object without modifying its structure, offering an alternative to subclassing.
* **Java I/O**:
  ```java
  // FileReader is wrapped inside BufferedReader to add buffer-reading capability
  BufferedReader reader = new BufferedReader(new FileReader("file.txt"));
  ```

---

### 38. State vs Strategy Pattern. What are the subtle differences?
* **Strategy Pattern**:
  * The client chooses the strategy algorithm explicitly at runtime.
  * Strategies are independent of one another.
* **State Pattern**:
  * The context changes its state automatically as actions are taken.
  * State classes contain transitions to other state classes (e.g., transition from `Ordered` -> `Shipped` -> `Delivered`).

---

### 39. Difference between Composition and Aggregation.
Both represent "Has-A" relationships:
* **Composition (Strong)**: Parent owns the child, and their lifecycles are tightly bound. If the parent is destroyed, the child is destroyed (e.g., `House` and `Room`).
* **Aggregation (Weak)**: Child can exist independently of the parent (e.g., `Department` and `Professor`).

---

### 40. How do you implement a thread-safe Singleton? Explain double-checked locking.
```java
public class ThreadSafeSingleton {
    // volatile is crucial to prevent instruction reordering during instantiation
    private static volatile ThreadSafeSingleton instance;
    
    private ThreadSafeSingleton() {} // Private constructor
    
    public static ThreadSafeSingleton getInstance() {
        if (instance == null) {                         // 1st check (no locking)
            synchronized (ThreadSafeSingleton.class) {  // Sync block
                if (instance == null) {                 // 2nd check (under lock)
                    instance = new ThreadSafeSingleton();
                }
            }
        }
        return instance;
    }
}
```

---

### 41. What is Dependency Injection? Why use a DI container like Spring?
* **DI**: A pattern where objects do not create their dependencies; instead, dependencies are supplied (injected) externally.
* **Why Spring DI**:
  * Reduces boilerplate code.
  * Decouples object creation from application logic.
  * Simplifies testing (allows easy mocking of interfaces).
  * Manages bean scope, lifecycle, and configuration centrally.

---

### 42. Explain the Builder pattern. Why is it preferred for objects with many optional parameters?
Provides a step-by-step creation flow for complex objects.
* **Why preferred**: Avoids "telescoping constructor" anti-pattern (constructors with many arguments, some null), makes code readable, and creates immutable instances.
```java
User user = new User.Builder()
                .firstName("John")
                .lastName("Doe")
                .age(30)
                .build();
```

---

### 43. What is the Adapter pattern?
Allows incompatible interfaces to work together. It wraps an existing class with a new interface that clients expect.
* *Example*: Wrapping an old legacy XML API to return JSON for a modern dashboard component.

---

### 44. Explain the Command pattern.
Encapsulates a request as an object, letting you parameterize clients with different requests, queue/log requests, and support undoable operations.
* *Example*: A remote control where each button is associated with a specific command object (e.g., `TurnOnLightCommand`).

---

### 45. What is a Flyweight pattern? How does Java's Integer cache use it?
Minimizes memory usage by sharing common state data across multiple similar objects.
* **Integer Cache**: Java pools `Integer` instances for values between `-128` and `127`. Calling `Integer.valueOf(10)` returns the same cached object reference, avoiding new allocations.
