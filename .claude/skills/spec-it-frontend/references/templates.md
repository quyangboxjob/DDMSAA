# Frontend integration-test case template

| Field | Content |
|---|---|
| Case ID | Feature-local test ID |
| Source | Current screen/business design, operationId, messageId |
| Preconditions | Authenticated role and fixture state from current specs |
| Action | User action and route |
| Network contract | OpenAPI method/path/request/response used by MSW |
| Expected result | UI state, navigation, accessibility and translated message |

Do not introduce a separate ID mapping unless one has been explicitly designed and added to the repository.
