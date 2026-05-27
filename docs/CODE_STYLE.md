# Code Style Guidelines

## Kotlin Style

MediaKit follows:
- Kotlin-first APIs
- Readable code
- Minimal public APIs
- Clear separation of responsibilities

## General Rules

- Prefer immutable data where possible
- Keep functions focused
- Avoid large god classes
- Prefer composition over deep inheritance

## SDK Priorities

Higher priority than feature count:
- Stability
- Readability
- Maintainability
- Bitmap safety
- Lifecycle correctness

## Public API Guidelines

- Keep APIs predictable
- Avoid leaking internal implementation
- Prefer builder-style APIs when useful
- Keep module boundaries clean
