# Release Process

## Initial Release Strategy

Current release target:
- JitPack

Future release target:
- Maven Central

## Recommended Release Flow

1. Merge feature PRs
2. Update CHANGELOG
3. Create version tag
4. Verify sample app
5. Publish release

## Semantic Versioning

Format:

```text
MAJOR.MINOR.PATCH
```

Example:

```text
1.0.0
```

## Stability Priorities

Before releases verify:
- Lifecycle safety
- Bitmap memory handling
- Crop accuracy
- API stability
- Dependency footprint
