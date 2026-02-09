# Design Principles

This document outlines key design principles that are fundamental to building robust, scalable, and maintainable software systems.

## SOLID Principles

SOLID is an acronym for five design principles intended to make software designs more understandable, flexible, and maintainable.

| Principle | Name | Description |
| :--- | :--- | :--- |
| **S** | Single Responsibility Principle (SRP) | A class should have only one reason to change. |
| **O** | Open/Closed Principle (OCP) | Software entities (classes, modules, functions) should be open for extension, but closed for modification. |
| **L** | Liskov Substitution Principle (LSP) | Subtypes must be substitutable for their base types. |
| **I** | Interface Segregation Principle (ISP) | No client should be forced to depend on methods it does not use. |
| **D** | Dependency Inversion Principle (DIP) | High-level modules should not depend on low-level modules. Both should depend on abstractions. |

---

### 1. Single Responsibility Principle (SRP)

A class should have one, and only one, reason to change. This means a class should only have a single job or responsibility.

**Benefits:**
- **Reduced Complexity:** Smaller, focused classes are easier to understand and maintain.
- **Improved Reusability:** Classes with a single responsibility are more likely to be reusable in other parts of the application.
- **Easier Testing:** It's simpler to write unit tests for a class that does one thing.

**Example:**

**Bad Practice:** A single class handles user data and also writes it to a file.
```java
class UserProfile {
    private String name;
    private String email;

    public void saveUserToFile() {
        // Logic to save user data to a file
    }
}
```
*This class has two responsibilities: managing user data and file I/O.*

**Good Practice:** Separate the responsibilities into two classes.
```java
class UserProfile {
    private String name;
    private String email;
    // Getters and setters
}

class UserRepository {
    public void save(UserProfile user) {
        // Logic to save user data to a file/database
    }
}
```
*Now, `UserProfile` only manages user data, and `UserRepository` handles persistence.*

---

### 2. Open/Closed Principle (OCP)

Software entities should be open for extension but closed for modification. This means you should be able to add new functionality without changing existing code.

**Benefits:**
- **Stability:** Reduces the risk of introducing bugs into existing, working code.
- **Flexibility:** Allows for new features to be added with minimal impact on the system.

**Example:**

**Bad Practice:** Modifying a class to add a new shape.
```java
class ShapeCalculator {
    public double calculateArea(Object shape) {
        if (shape instanceof Rectangle) {
            // Area calculation for Rectangle
        }
        if (shape instanceof Circle) { // New shape requires modification
            // Area calculation for Circle
        }
    }
}
```
*Adding a `Circle` requires modifying the `ShapeCalculator` class.*

**Good Practice:** Use an interface and polymorphism.
```java
interface Shape {
    double getArea();
}

class Rectangle implements Shape {
    public double getArea() { /* ... */ }
}

class Circle implements Shape {
    public double getArea() { /* ... */ }
}

class ShapeCalculator {
    public double calculateArea(Shape shape) {
        return shape.getArea();
    }
}
```
*To add a new shape (e.g., `Triangle`), you just create a new class implementing `Shape`. `ShapeCalculator` remains unchanged.*

---

### 3. Liskov Substitution Principle (LSP)

If S is a subtype of T, then objects of type T may be replaced with objects of type S without altering any of the desirable properties of the program.

In simpler terms, a subclass should be able to replace its parent class without causing errors.

**Benefits:**
- **Reliability:** Ensures that a subclass can be used wherever its superclass is expected.
- **Code Reusability:** Promotes the use of inheritance correctly.

**Example:**

**Bad Practice:** A subclass changes the behavior of the parent class in an unexpected way.
```java
class Bird {
    public void fly() { /* ... */ }
}

class Ostrich extends Bird {
    @Override
    public void fly() {
        throw new UnsupportedOperationException("Ostriches can't fly");
    }
}
```
*If a client expects any `Bird` to fly, using an `Ostrich` object will break the program.*

**Good Practice:** Create a more appropriate hierarchy.
```java
class Bird {
    // Common bird properties
}

class FlyingBird extends Bird {
    public void fly() { /* ... */ }
}

class Ostrich extends Bird {
    // Ostrich-specific behaviors, but no fly() method
}
```
*This design correctly models the abilities of different birds.*

---

### 4. Interface Segregation Principle (ISP)

Clients should not be forced to depend on interfaces they do not use. This means it's better to have many small, specific interfaces than one large, general-purpose interface.

**Benefits:**
- **Decoupling:** Reduces the impact of changes. If an interface changes, only the clients that use that specific interface need to be updated.
- **Improved Cohesion:** Interfaces are more focused and easier to understand.

**Example:**

**Bad Practice:** A single "fat" interface for a worker.
```java
interface Worker {
    void work();
    void eat();
}

class Human implements Worker {
    public void work() { /* ... */ }
    public void eat() { /* ... */ }
}

class Robot implements Worker {
    public void work() { /* ... */ }
    public void eat() {
        // Robots don't eat, so this method is irrelevant
    }
}
```
*`Robot` is forced to implement the `eat` method, which it doesn't need.*

**Good Practice:** Segregate the interface.
```java
interface Workable {
    void work();
}

interface Eatable {
    void eat();
}

class Human implements Workable, Eatable {
    public void work() { /* ... */ }
    public void eat() { /* ... */ }
}

class Robot implements Workable {
    public void work() { /* ... */ }
}
```
*Now, classes only implement the interfaces relevant to them.*

---

### 5. Dependency Inversion Principle (DIP)

1.  High-level modules should not depend on low-level modules. Both should depend on abstractions (e.g., interfaces).
2.  Abstractions should not depend on details. Details (concrete implementations) should depend on abstractions.

**Benefits:**
- **Loose Coupling:** Reduces dependencies between modules, making the system more flexible.
- **Testability:** Makes it easier to substitute dependencies for testing (e.g., using mock objects).

**Example:**

**Bad Practice:** A high-level class depends directly on a low-level class.
```java
class LightBulb {
    public void turnOn() { /* ... */ }
    public void turnOff() { /* ... */ }
}

class Switch {
    private LightBulb bulb; // Direct dependency on a concrete class

    public Switch() {
        this.bulb = new LightBulb();
    }

    public void operate() {
        // logic to turn on/off
    }
}
```
*`Switch` is tightly coupled to `LightBulb`. If you want to use `Switch` with a different kind of light, you have to change the `Switch` class.*

**Good Practice:** Depend on an abstraction.
```java
interface Switchable {
    void turnOn();
    void turnOff();
}

class LightBulb implements Switchable {
    public void turnOn() { /* ... */ }
    public void turnOff() { /* ... */ }
}

class Switch {
    private Switchable device;

    public Switch(Switchable device) {
        this.device = device;
    }

    public void operate() {
        // logic to turn on/off
    }
}
```
*Now, `Switch` depends on the `Switchable` interface and can work with any device that implements it.*

---

## Other Key Principles

### KISS (Keep It Simple, Stupid)
The KISS principle states that most systems work best if they are kept simple rather than made complicated; therefore, simplicity should be a key goal in design, and unnecessary complexity should be avoided.

### DRY (Don't Repeat Yourself)
"Every piece of knowledge must have a single, unambiguous, authoritative representation within a system." This principle is aimed at reducing repetition of software patterns, replacing it with abstractions or using data normalization to avoid redundancy.

### YAGNI (You Ain't Gonna Need It)
YAGNI is a principle of extreme programming (XP) that states a programmer should not add functionality until deemed necessary. It's a reminder to avoid over-engineering and gold-plating.

## Comparison of Principles

| Principle | Core Idea | Primary Goal |
| :--- | :--- | :--- |
| **SOLID** | A set of five principles for object-oriented design. | Create maintainable, flexible, and understandable systems. |
| **KISS** | Avoid unnecessary complexity. | Simplicity and clarity. |
| **DRY** | Avoid code duplication. | Reduce redundancy and improve maintainability. |
| **YAGNI** | Implement features only when you need them. | Avoid wasted effort on unnecessary features. |

---

## DI (Dependency Injection)

Dependency Injection (DI) is a specific implementation of the Dependency Inversion Principle (the 'D' in SOLID). It is a technique where an object receives other objects that it depends on, rather than creating them internally.

This approach decouples the creation of objects from their behavior, making the code more modular, testable, and maintainable.

### How Dependency Injection Works

At its core, DI involves:
- **Dependencies:** The objects or resources a class needs to function.
- **Injection:** The process of passing these dependencies to the dependent object from an external source.

DI can be implemented in three main ways:
1.  **Constructor Injection:** Dependencies are provided through a class constructor. (Most common and recommended)
2.  **Setter Injection:** Dependencies are passed through public setter methods.
3.  **Interface Injection:** The dependency provides an injector method that the dependent class uses to receive its dependencies (less common).

### Example

**Without DI:**
```java
public class OrderService {
    private final Database a-system\atlas = new OracleDatabase(); // Tight coupling

    public void processOrder(Order order) {
        a-system\atlas.save(order);
    }
}
```

**With DI (Constructor Injection):**
```java
public class OrderService {
    private final Database a-system\atlas;

    public OrderService(Database a-system\atlas) { // Dependency is injected
        this.a-system\atlas = a-system\atlas;
    }

    public void processOrder(Order order) {
        a-system\atlas.save(order);
    }
}
```
Here, the `Database` object is provided from the outside, making `OrderService` independent of how the database is created. You could easily swap `OracleDatabase` with `PostgresDatabase` without changing `OrderService`.

### Advantages of DI

| Advantage | Description |
| :--- | :--- |
| **Loose Coupling** | Classes are not responsible for creating their dependencies, making the system modular. |
| **Improved Testability** | Dependencies can be easily mocked or stubbed for unit testing. |
| **Easier Maintenance** | Changing a dependency (e.g., swapping a database) only requires updating the DI configuration, not the class itself. |
| **Reusability** | Dependencies can be shared and reused across different classes. |
| **Configuration Flexibility**| Allows for dynamic injection of dependencies based on runtime environments (e.g., dev vs. prod). |
| **Adherence to SOLID** | Directly implements the Dependency Inversion Principle and supports the Single Responsibility Principle. |