# MediaKit Android

A modular Android media SDK built with Kotlin. Pick images from the gallery or camera, then crop them — all with a single fluent API.

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

    // Optional — add only if you need the built-in crop UI
    implementation("io.github.akshayashokcode:imagecropper:0.1.0")
}
```

`imagecropper` depends on `imagepicker` transitively, so adding just `imagecropper` is enough if you want both.

---

## Quick Start

### Pick from gallery (no crop)

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
                // ImagePickerException: PermissionDenied, AppNotFound, etc.
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
    .onResult { result -> /* result.uri is the cropped image */ }
```

### Camera capture

```kotlin
val picker = ImagePicker.with(this, this)
    .source(MediaSource.Camera)     // requests CAMERA permission automatically
    .onResult { result ->
        when (result) {
            is ImagePickerResult.SuccessWithBitmap -> { /* orientation-corrected bitmap + uri */ }
            else -> Unit
        }
    }
```

### Embed `CropperView` standalone (optional)

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

---

## Media Sources

```kotlin
MediaSource.Gallery  // system photo picker / file picker
MediaSource.Camera   // camera capture (requests CAMERA permission automatically)
MediaSource.Both     // falls back to gallery (chooser UI planned)
```

## Result Types

```kotlin
sealed class ImagePickerResult {
    data class Success(val uri: Uri)                               // gallery pick or crop output
    data class SuccessWithBitmap(val uri: Uri, val bitmap: Bitmap) // camera capture (orientation-corrected)
    data object Cancelled                                          // user dismissed
    data class Error(val message: String)                          // non-fatal error
}
```

## Exception Types

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

- minSdk 24 (Android 7.0)
- Kotlin

---

## Project Structure

```
MediaKit-android/
├── imagecropper/      (library — CropperView, CropperActivity, MediaKitCropProvider)
├── imagepicker/       (library — ImagePicker, ImageCropProvider interface)
├── sample-app/        (demo Compose app)
└── docs/
```

## Goals

- Modular — import only what you need; `imagepicker` works without `imagecropper` on the classpath
- Lightweight — no third-party image loading dependencies
- Lifecycle-safe — all `registerForActivityResult` calls happen before `onStart`
- Kotlin-first — sealed results, fluent builder, no Java boilerplate
- Extensible — implement `ImageCropProvider` to plug in any crop library

## License

MIT
