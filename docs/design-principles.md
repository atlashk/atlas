# Design Principles

## DI

Dependency Injection (DI) is a technique used in object-oriented programming to achieve Inversion of Control (IoC).

It involves providing an object (called the dependent object) with its dependencies (other objects it depends on) from the outside, instead of the dependent object instantiating them itself.

This approach decouples the creation of objects from their behavior, making the code more modular, testable, and maintainable.

### Dependency Injection Working

At its core, DI involves:
- Dependencies: The objects or resources a class needs to function.
- Injection: The process of passing these dependencies to the dependent object.

DI can be implemented in three main ways:
1. Constructor Injection: Dependencies are passed to the object via its constructor.
2. Setter Injection: Dependencies are set through public setter methods.
3. Interface Injection: The dependency provides an injector method that the dependent class uses to receive its dependencies (less common).

Example

Without DI:

```java
public class Service {
    private Repository repository = new Repository(); // Tight coupling

    public void performService() {
        repository.save();
    }
}
```

With DI:

```java
public class Service {
    private Repository repository;

    public Service(Repository repository) { // Constructor Injection
        this.repository = repository;
    }
    public void performService() {
        repository.save();
    }
}
```

Here, the Repository object is passed from outside, making Service independent of its creation logic.

Advantages

1. Loose Coupling: Classes are no longer responsible for creating their own dependencies, which makes the code more modular and easier to manage.
2. Improved Testability: Dependencies can be easily mocked or stubbed for unit testing, as they are passed from outside rather than being hardcoded inside the class.
3. Easier Maintenance: If a dependency changes (e.g., swapping a database implementation), you only need to update the dependency injection configuration, not the dependent class.
4. Reusability: Dependencies can be reused across different classes, promoting a more DRY (Don’t Repeat Yourself) codebase.
5. Scalability: Adding new functionality or dependencies becomes easier, as the code is modular and not tightly coupled.
6. Adherence to SOLID Principles:
- Promotes the Single Responsibility Principle by separating the creation of dependencies from the use of dependencies.
- Encourages the Dependency Inversion Principle, as classes depend on abstractions (interfaces) rather than concrete implementations.
7. Configuration Flexibility: DI allows dynamic injection of dependencies based on runtime configurations, making the code adaptable to different environments (e.g., development, production).
8. Better Code Readability: By delegating dependency management to a DI container or framework, the main code becomes cleaner and more focused on business logic.