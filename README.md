# MediaKit Android

A modular Android media SDK built with Kotlin. Pick images from the gallery or camera, then crop them — all with a single fluent API.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.akshayashokcode/imagepicker?label=imagepicker)](https://central.sonatype.com/artifact/io.github.akshayashokcode/imagepicker)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.akshayashokcode/imagecropper?label=imagecropper)](https://central.sonatype.com/artifact/io.github.akshayashokcode/imagecropper)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)

---

## Modules

| Artifact | Description |
|----------|-------------|
| `io.github.akshayashokcode:imagepicker` | Gallery & camera image picking with lifecycle-safe activity result handling |
| `io.github.akshayashokcode:imagecropper` | Custom crop view with touch-driven resize, rule-of-thirds grid, and state restoration |

---

## Installation

Add to your app's `build.gradle.kts`:

```kotlin
dependencies {
    // Image picking (gallery + camera)
    implementation("io.github.akshayashokcode:imagepicker:0.1.0")

    // Optional — only needed if you want the built-in crop UI
    implementation("io.github.akshayashokcode:imagecropper:0.1.0")
}
```

> `imagecropper` depends on `imagepicker` transitively — adding just `imagecropper` is enough if you want both.

---

## Quick Start

### Pick from gallery

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Must be constructed before setContent — registerForActivityResult
        // must be called before the activity reaches STARTED.
        val picker = ImagePicker.with(this, this)
            .source(MediaSource.Gallery)
            .onResult { result ->
                when (result) {
                    is ImagePickerResult.Success -> { /* use result.uri */ }
                    is ImagePickerResult.Cancelled -> { /* user cancelled */ }
                    is ImagePickerResult.Error -> { /* show result.message */ }
                    else -> Unit
                }
            }
            .onError { error ->
                // ImagePickerException subtypes: PermissionDenied, AppNotFound, etc.
            }

        setContent {
            MyTheme {
                MyScreen(onPickImage = { picker.launch() })
            }
        }
    }
}
```

### Pick and crop

```kotlin
val picker = ImagePicker.with(this, this)
    .source(MediaSource.Gallery)
    .crop(MediaKitCropProvider())   // requires imagecropper artifact
    .onResult { result ->
        // result.uri is the cropped image URI
    }
```

### Camera capture

```kotlin
val picker = ImagePicker.with(this, this)
    .source(MediaSource.Camera)     // CAMERA permission requested automatically
    .onResult { result ->
        when (result) {
            is ImagePickerResult.SuccessWithBitmap -> {
                // result.uri — content URI
                // result.bitmap — orientation-corrected bitmap
            }
            else -> Unit
        }
    }
```

### Embed `CropperView` standalone

Use the crop UI directly in your own layout without the picker flow:

```xml
<com.akshayashokcode.imagecropper.CropperView
    android:id="@+id/cropperView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

```kotlin
cropperView.setImageBitmap(bitmap)
val cropped: Bitmap? = cropperView.getCroppedImage()
```

### Bring your own crop library

Implement `ImageCropProvider` to plug in uCrop, Android Image Cropper, or any other crop library:

```kotlin
class MyCropProvider : ImageCropProvider {
    override fun createLauncher(
        context: Context,
        caller: ActivityResultCaller,
        callback: (ImagePickerResult) -> Unit
    ): CropLauncher {
        // register your activity result launcher here
        // return CropLauncher { uri -> /* launch your crop UI */ }
    }
}

val picker = ImagePicker.with(this, this)
    .source(MediaSource.Gallery)
    .crop(MyCropProvider())
    .onResult { result -> }
```

---

## API Reference

### Media Sources

```kotlin
MediaSource.Gallery  // system photo picker / file picker
MediaSource.Camera   // camera capture (CAMERA permission requested automatically)
MediaSource.Both     // falls back to gallery (chooser UI planned)
```

### Result Types

```kotlin
sealed class ImagePickerResult {
    data class Success(val uri: Uri)                                // gallery pick or crop output
    data class SuccessWithBitmap(val uri: Uri, val bitmap: Bitmap) // camera capture (orientation-corrected)
    data object Cancelled                                           // user dismissed
    data class Error(val message: String)                           // non-fatal error
}
```

### Exception Types

```kotlin
sealed class ImagePickerException {
    object PermissionDenied     // CAMERA permission not granted
    object AppNotFound          // no camera or gallery app available
    object FileCreationFailed   // temp file for camera capture could not be created
    object InvalidUri           // returned URI was null or unreadable
    object DecodingFailed       // bitmap decode / EXIF rotation failed
    object FileDeletionFailed   // cleanup of temp file failed
    object IntentFailed         // intent could not be launched
    class  Unknown(message: String?, cause: Throwable?)
}
```

---

## Requirements

- minSdk 24 (Android 7.0+)
- Kotlin

---

## Project Structure

```
MediaKit-android/
├── imagepicker/       (library — ImagePicker, ImageCropProvider interface)
├── imagecropper/      (library — CropperView, CropperActivity, MediaKitCropProvider)
├── sample-app/        (demo Compose app)
└── docs/
```

## Design Goals

- **Modular** — `imagepicker` works without `imagecropper` on the classpath
- **Lightweight** — no third-party image loading dependencies
- **Lifecycle-safe** — all `registerForActivityResult` calls happen before `onStart`
- **Kotlin-first** — sealed results, fluent builder, no Java boilerplate
- **Extensible** — implement `ImageCropProvider` to plug in any crop library

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

MIT — see [LICENSE](LICENSE).
