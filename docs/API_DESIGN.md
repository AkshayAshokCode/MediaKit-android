# API Design Philosophy

## Goals

MediaKit APIs should be:
- Simple
- Kotlin-first
- Lifecycle-aware
- Modular
- Lightweight
- Easy to integrate

## Principles

### Minimal Public APIs
Expose only what developers need.

### Modular Design
Each module should work independently.

### Modern Android APIs
Prefer Activity Result APIs, coroutines, and modern Android patterns.

### Stability First
Reliability and bitmap safety are higher priority than feature count.
