# Backend unit-test generation checklist

- The target is an existing common/infrastructure class, not feature business code assigned to `spec-wf-testgen`.
- Constructors, methods, configuration keys, and collaborators match actual source.
- Mocks isolate external boundaries while assertions verify observable behavior.
- Authentication data and roles originate from the current auth/API design.
- ThreadLocal, static state, files, and mocks are cleaned up after each test when applicable.
