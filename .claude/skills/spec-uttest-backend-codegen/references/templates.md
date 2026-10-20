# Backend unit-test code template guidance

```java
@Test
void method_returnsExpectedResult_whenConditionIsMet() {
    // Arrange: use values and collaborators from the current implementation.
    // Act
    // Assert: verify observable output, state, or contract.
}
```

For authorization tests, obtain subject, role, and protected operation from the current authentication and API specifications. Do not copy identity or role examples from another project.
