# JitPack Setup

## Publishing Goal

MediaKit will initially use JitPack for distribution.

## Planned Dependencies

### imagepicker

```kotlin
implementation("com.github.AkshayAshokCode.MediaKit-android:imagepicker:<version>")
```

### imagecropper

```kotlin
implementation("com.github.AkshayAshokCode.MediaKit-android:imagecropper:<version>")
```

## Publishing Flow

1. Push changes to GitHub
2. Create Git tag
3. JitPack builds artifacts
4. Consumers use tagged version

## Long-Term Direction

Future target:
- Maven Central publishing
