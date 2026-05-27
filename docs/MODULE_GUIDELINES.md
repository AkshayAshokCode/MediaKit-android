# Module Guidelines

## Goals

Every MediaKit module should:
- Be independently usable
- Avoid unnecessary dependencies
- Expose minimal public APIs
- Follow modern Android development practices

## Recommended Structure

```text
module/
├── api/
├── internal/
├── model/
├── util/
└── ui/
```

## API Rules

- Prefer Kotlin APIs
- Use lifecycle-aware components
- Keep APIs simple
- Avoid leaking implementation details
- Prefer Activity Result APIs

## Stability Priorities

Higher priority than feature count:
- Bitmap safety
- Lifecycle handling
- Memory management
- Error handling
- Developer experience
