# Backend unit-test specification checklist

- Target class and behavior exist in the current source tree.
- Each case identifies a design, API, schema, or implementation source.
- Setup uses current configuration and test fixtures only.
- Authentication cases derive subject and role from the current auth design.
- Normal, error, boundary, and cleanup behavior are covered where applicable.
- Feature business-code cases are delegated to the feature test pipeline when a detailed-design test list exists.
