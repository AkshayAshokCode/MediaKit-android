# MediaKit Android

A modular Android media SDK built with Kotlin. Each module is independently installable — add only what your app needs.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.akshayashokcode/imagepicker?label=imagepicker)](https://central.sonatype.com/artifact/io.github.akshayashokcode/imagepicker)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.akshayashokcode/imagecropper?label=imagecropper)](https://central.sonatype.com/artifact/io.github.akshayashokcode/imagecropper)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.akshayashokcode/mediapicker?label=mediapicker)](https://central.sonatype.com/artifact/io.github.akshayashokcode/mediapicker)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.akshayashokcode/imagecompressor?label=imagecompressor)](https://central.sonatype.com/artifact/io.github.akshayashokcode/imagecompressor)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.akshayashokcode/audiorecorder?label=audiorecorder)](https://central.sonatype.com/artifact/io.github.akshayashokcode/audiorecorder)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.akshayashokcode/videocompressor?label=videocompressor)](https://central.sonatype.com/artifact/io.github.akshayashokcode/videocompressor)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.akshayashokcode/mediapreviewer?label=mediapreviewer)](https://central.sonatype.com/artifact/io.github.akshayashokcode/mediapreviewer)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)

---

## Which module do I need?

| I want to… | Module |
|---|---|
| Let the user pick a photo from gallery or camera | `imagepicker` |
| Crop a photo after picking | `imagecropper` |
| Let the user pick images, videos, audio, or documents | `mediapicker` |
| Resize or re-encode an image (no UI) | `imagecompressor` |
| Record audio inside the app | `audiorecorder` |
| Compress a video (no UI) | `videocompressor` |
| Show a fullscreen swipe-through preview of media | `mediapreviewer` |
| Use multiple modules and manage versions in one place | `mediakit-bom` |

---

## Modules

| Artifact | What it does |
|---|---|
| `imagepicker` | Gallery and camera image picking with lifecycle-safe activity result handling |
| `imagecropper` | Touch crop UI with aspect ratio lock, shape mask, rotate/flip, and configurable output. Migrating from ArthurHub? See [MIGRATION.md](MIGRATION.md). |
| `mediapicker` | Unified storage picker for images, videos, audio, and documents |
| `imagecompressor` | Coroutine-based image resize and re-encode — no `ActivityResultCaller` required |
| `audiorecorder` | In-app audio recording with a waveform visualisation and timer |
| `videocompressor` | Coroutine-based video compression — no `ActivityResultCaller` required |
| `mediapreviewer` | Fullscreen swipe-between-items preview for images, video, and audio |
| `mediakit-bom` | Bill of Materials — import once to align all module versions automatically |

---

## Installation

### Option A — BOM (recommended when using multiple modules)

The BOM pins all module versions so you never have version mismatches.

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(platform("io.github.akshayashokcode:mediakit-bom:1.0.0"))

    // Add only the modules you need — no version number required
    implementation("io.github.akshayashokcode:imagepicker")
    implementation("io.github.akshayashokcode:imagecropper")
    implementation("io.github.akshayashokcode:mediapicker")
    implementation("io.github.akshayashokcode:imagecompressor")
    implementation("io.github.akshayashokcode:audiorecorder")
    implementation("io.github.akshayashokcode:videocompressor")
    implementation("io.github.akshayashokcode:mediapreviewer")
}
```

### Option B — individual modules

```kotlin
dependencies {
    implementation("io.github.akshayashokcode:imagepicker:1.0.0")
    implementation("io.github.akshayashokcode:imagecropper:1.0.0")   // includes imagepicker transitively
    implementation("io.github.akshayashokcode:mediapicker:1.0.0")
    implementation("io.github.akshayashokcode:imagecompressor:1.0.0")
    implementation("io.github.akshayashokcode:audiorecorder:1.0.0")
    implementation("io.github.akshayashokcode:videocompressor:1.0.0")
    implementation("io.github.akshayashokcode:mediapreviewer:1.0.0")
}
```

---

## Quick Start

### Pick an image from gallery

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Must be constructed before setContent
        val picker = ImagePicker.with(this, this)
            .source(MediaSource.Gallery)
            .onResult { result ->
                when (result) {
                    is ImagePickerResult.Success -> { /* use result.uri */ }
                    is ImagePickerResult.Cancelled -> { }
                    is ImagePickerResult.Error -> { /* show result.message */ }
                    else -> Unit
                }
            }

        setContent {
            MyTheme {
                Button(onClick = { picker.launch() }) { Text("Pick Image") }
            }
        }
    }
}
```

**Compose API** (no `onCreate`-before-`setContent` restriction):

```kotlin
@OptIn(ExperimentalMediaKitApi::class)
@Composable
fun MyScreen() {
    val picker = rememberImagePicker(source = MediaSource.Gallery) { result -> }
    Button(onClick = { picker.launch() }) { Text("Pick Image") }
}
```

### Pick from gallery or camera (let user choose)

```kotlin
val picker = ImagePicker.with(this, this)
    .source(MediaSource.Both)   // shows a dialog: Gallery / Camera
    .onResult { result -> }
```

### Pick and crop

```kotlin
val picker = ImagePicker.with(this, this)
    .source(MediaSource.Gallery)
    .crop(MediaKitCropProvider())   // requires imagecropper module
    .onResult { result ->
        // result.uri is the cropped image URI
    }
```

### Pick and crop with options

```kotlin
val picker = ImagePicker.with(this, this)
    .source(MediaSource.Gallery)
    .crop(
        MediaKitCropProvider(
            CropperOptions(
                aspectRatios = listOf(AspectRatio.Free, AspectRatio.Square, AspectRatio.SixteenNine),
                showRotateButtons = true,
                showFlipButtons = true,
                cropShape = CropShape.Circle,
                outputFormat = OutputFormat.PNG,
                maxOutputWidth = 2048,
                maxOutputHeight = 2048
            )
        )
    )
    .onResult { result -> }
```

### Capture a photo from camera

```kotlin
val picker = ImagePicker.with(this, this)
    .source(MediaSource.Camera)   // CAMERA permission requested automatically
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

### Pick any media type (images, video, audio, documents)

```kotlin
// Construct before setContent
val picker = MediaPicker.with(this, this)
    .onResult { result ->
        when (result) {
            is MediaPickerResult.Success -> { /* result.item: MediaItem */ }
            is MediaPickerResult.MultipleSuccess -> { /* result.items: List<MediaItem> */ }
            is MediaPickerResult.Cancelled -> { }
            is MediaPickerResult.Error -> { }
        }
    }

// At launch time — set options and launch
picker.mediaTypes(MediaType.Image, MediaType.Video).allowMultiple(true).launch()
```

**With restrictions:**

```kotlin
MediaPicker.with(this, this)
    .mediaTypes(MediaType.Image, MediaType.Video)
    .restrictMimeTypes("image/gif", "video/webm")
    .restrictExtensions("apk")
    .allowMultiple(true)
    .onResult { result -> }
    .onError { error -> }
    .launch()
```

**Compose API:**

```kotlin
@OptIn(ExperimentalMediaKitApi::class)
@Composable
fun MyScreen() {
    val picker = rememberMediaPicker(
        MediaType.Image, MediaType.Video,
        allowMultiple = true
    ) { result -> }
    Button(onClick = { picker.launch() }) { Text("Pick Media") }
}
```

### Compress an image

`ImageCompressor` does not need `ActivityResultCaller` — construct it anywhere, including inside a Composable.

**Callback-based:**

```kotlin
ImageCompressor.with(context)
    .source(uri)
    .options(CompressionOptions(maxWidth = 1920, quality = 85))
    .onResult { result ->
        when (result) {
            is ImageCompressionResult.Success -> {
                // result.uri — compressed file in app cacheDir
                // result.originalSizeBytes / result.compressedSizeBytes
            }
            is ImageCompressionResult.Error -> { }
            else -> Unit
        }
    }
    .compressAsync()
```

**Suspend API (inside a coroutine or `LaunchedEffect`):**

```kotlin
val result = ImageCompressor.with(context)
    .source(uri)
    .options(CompressionOptions(maxWidth = 1920, quality = 85))
    .compress()   // suspend fun — runs on Dispatchers.IO
```

### Record audio

`AudioRecorder` launches a built-in recording screen with waveform and timer. Requires `ActivityResultCaller`.

```kotlin
val recorder = AudioRecorder.with(this, this)
    .options(
        AudioRecorderOptions(
            maxDurationSeconds = 120,
            format = AudioOutputFormat.AAC_M4A,
            showWaveform = true
        )
    )
    .onResult { result ->
        when (result) {
            is AudioRecorderResult.Success -> {
                // result.uri — recorded audio URI
                // result.durationMs
            }
            is AudioRecorderResult.Cancelled -> { }
            is AudioRecorderResult.Error -> { }
        }
    }

// In a click handler:
recorder.launch()   // RECORD_AUDIO permission requested automatically
```

### Compress a video

`VideoCompressor` does not need `ActivityResultCaller`. Call `cancel()` to abort in-progress compression.

**Callback-based:**

```kotlin
val compressor = VideoCompressor.with(context)
    .source(uri)
    .options(VideoCompressionOptions(maxWidth = 1280, maxHeight = 720, videoBitrateBps = 2_000_000))
    .onProgress { percent -> /* update progress bar */ }
    .onResult { result ->
        when (result) {
            is VideoCompressionResult.Success -> {
                // result.uri — compressed video URI in app cacheDir
                // result.originalSizeBytes / result.compressedSizeBytes
            }
            is VideoCompressionResult.Cancelled -> { }
            is VideoCompressionResult.Error -> { }
        }
    }

compressor.compressAsync()   // returns Job — observe or cancel later
```

**Suspend API:**

```kotlin
val result = VideoCompressor.with(context)
    .source(uri)
    .options(VideoCompressionOptions(maxWidth = 1280))
    .compress()   // suspend fun — runs on Dispatchers.IO
```

**Cancel in-progress compression:**

```kotlin
compressor.cancel()
```

### Preview media fullscreen

```kotlin
MediaPreviewer.with(context, this)
    .items(
        listOf(
            MediaPreviewItem.Image(imageUri),
            MediaPreviewItem.Video(videoUri),
            MediaPreviewItem.Audio(audioUri)
        )
    )
    .options(PreviewOptions(showShareButton = true, zoomEnabled = true))
    .launch()
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

```kotlin
class MyCropProvider : ImageCropProvider {
    override fun createLauncher(
        context: Context,
        caller: ActivityResultCaller,
        callback: (ImagePickerResult) -> Unit
    ): CropLauncher {
        // register your activity result launcher and return CropLauncher
    }
}

val picker = ImagePicker.with(this, this)
    .source(MediaSource.Gallery)
    .crop(MyCropProvider())
    .onResult { result -> }
```

---

## API Reference

### imagepicker

#### Media Sources

```kotlin
MediaSource.Gallery  // system photo picker
MediaSource.Camera   // camera capture (CAMERA permission requested automatically)
MediaSource.Both     // shows a dialog: Gallery / Camera
```

#### Result Types

```kotlin
sealed class ImagePickerResult {
    data class Success(val uri: Uri)                                // gallery pick or crop output
    data class SuccessWithBitmap(val uri: Uri, val bitmap: Bitmap) // camera capture (orientation-corrected)
    data object Cancelled
    data class Error(val message: String)
}
```

#### Exception Types

```kotlin
sealed class ImagePickerException {
    object PermissionDenied
    object AppNotFound
    object FileCreationFailed
    object InvalidUri
    object DecodingFailed
    object FileDeletionFailed
    object IntentFailed
    class  Unknown(message: String?, cause: Throwable?)
}
```

---

### imagecropper

> Migrating from `com.github.ArthurHub:android-image-cropper`? See [MIGRATION.md](MIGRATION.md).

#### CropperOptions

```kotlin
CropperOptions(
    aspectRatios: List<AspectRatio> = listOf(AspectRatio.Free),
    lockAspectRatio: Boolean = false,
    cropShape: CropShape = CropShape.Rectangle,
    showRotateButtons: Boolean = false,
    showFlipButtons: Boolean = false,
    outputFormat: OutputFormat = OutputFormat.JPEG(),
    maxOutputWidth: Int = 0,    // 0 = no limit
    maxOutputHeight: Int = 0,
    minOutputWidth: Int = 100,
    minOutputHeight: Int = 100
)
```

#### AspectRatio

```kotlin
AspectRatio.Free                  // unconstrained (default)
AspectRatio.Square                // 1:1
AspectRatio.FourThree             // 4:3
AspectRatio.SixteenNine           // 16:9
AspectRatio.ThreeTwo              // 3:2
AspectRatio.FiveFour              // 5:4
AspectRatio.Ratio(width, height)  // custom
```

#### CropShape

```kotlin
CropShape.Rectangle   // default
CropShape.Circle      // circular mask — output has transparent corners
```

#### OutputFormat

```kotlin
OutputFormat.JPEG(quality: Int = 90)   // default
OutputFormat.PNG                        // lossless; use with Circle crop for transparency
OutputFormat.WebP(quality: Int = 90)
```

---

### mediapicker

#### MediaType

```kotlin
sealed class MediaType {
    object Image
    object Video
    object Audio
    object Document   // PDF, Word, Excel, plain text
    object All        // default — all of the above
}
```

#### Contract selection

| Types requested | Android API used |
|---|---|
| Image only | `PickVisualMedia(ImageOnly)` |
| Video only | `PickVisualMedia(VideoOnly)` |
| Image + Video | `PickVisualMedia(ImageAndVideo)` |
| Audio / Document / mixed | `OpenDocument(mimeTypes)` |
| All | `OpenDocument("*/*")` |

#### Result Types

```kotlin
sealed class MediaPickerResult {
    data class Success(val item: MediaItem)
    data class MultipleSuccess(val items: List<MediaItem>)
    data object Cancelled
    data class Error(val message: String)
}

sealed class MediaItem {
    data class Image(val uri: Uri, val mimeType: String)
    data class Video(val uri: Uri, val mimeType: String, val durationMs: Long)
    data class Audio(val uri: Uri, val mimeType: String, val durationMs: Long, val displayName: String)
    data class Document(val uri: Uri, val mimeType: String, val displayName: String, val sizeBytes: Long)
    data class Unknown(val uri: Uri, val mimeType: String)
}
```

#### Exception Types

```kotlin
sealed class MediaPickerException {
    object AppNotFound
    object InvalidUri
    object RestrictedFile    // blocked by restrictMimeTypes / restrictExtensions
    class  Unknown(message: String, cause: Throwable?)
}
```

---

### imagecompressor

#### CompressionOptions

```kotlin
CompressionOptions(
    maxWidth: Int = 1920,
    maxHeight: Int = 1920,
    quality: Int = 85,                  // JPEG/WebP quality 0–100
    format: Bitmap.CompressFormat = JPEG,
    maxFileSizeBytes: Long? = null,     // iterates quality down to 30 to hit target
    preserveExif: Boolean = false
)
```

#### Result Types

```kotlin
sealed class ImageCompressionResult {
    data class Success(val uri: Uri, val originalSizeBytes: Long, val compressedSizeBytes: Long)
    data object Cancelled
    data class Error(val message: String)
}
```

#### Exception Types

```kotlin
sealed class ImageCompressionException {
    object InvalidSource
    object DecodingFailed
    object EncodingFailed
    object FileCreationFailed
    class  Unknown(message: String, cause: Throwable?)
}
```

---

### audiorecorder

#### AudioRecorderOptions

```kotlin
AudioRecorderOptions(
    maxDurationSeconds: Int = 0,                     // 0 = no limit
    format: AudioOutputFormat = AudioOutputFormat.AAC_M4A,
    showWaveform: Boolean = true
)
```

#### Result Types

```kotlin
sealed class AudioRecorderResult {
    data class Success(val uri: Uri, val durationMs: Long)
    data object Cancelled
    data class Error(val message: String)
}
```

---

### videocompressor

#### VideoCompressionOptions

```kotlin
VideoCompressionOptions(
    maxWidth: Int = 1280,
    maxHeight: Int = 720,
    videoBitrateBps: Int = 2_000_000,
    frameRate: Int = 30
)
```

#### Result Types

```kotlin
sealed class VideoCompressionResult {
    data class Success(val uri: Uri, val originalSizeBytes: Long, val compressedSizeBytes: Long)
    data object Cancelled
    data class Error(val message: String)
}
```

---

### mediapreviewer

#### MediaPreviewItem

```kotlin
sealed class MediaPreviewItem {
    data class Image(val uri: Uri)
    data class Video(val uri: Uri)
    data class Audio(val uri: Uri)
}
```

#### PreviewOptions

```kotlin
PreviewOptions(
    showShareButton: Boolean = false,
    zoomEnabled: Boolean = true
)
```

---

## ActivityResultCaller requirement

Modules that open system pickers or activities must register `ActivityResultLauncher` before the activity reaches `STARTED`. Construct them in `Activity.onCreate` before `setContent`.

| Module | Needs `ActivityResultCaller`? |
|---|---|
| `imagepicker` | Yes — construct in `onCreate` before `setContent` |
| `imagecropper` | Yes (via `imagepicker`) |
| `mediapicker` | Yes — construct in `onCreate` before `setContent` |
| `audiorecorder` | Yes — construct in `onCreate` before `setContent` |
| `imagecompressor` | No — construct anywhere, including inside a Composable |
| `videocompressor` | No — construct anywhere, including inside a Composable |
| `mediapreviewer` | No — uses `startActivity` directly |

**Compose workaround** — use `rememberImagePicker` / `rememberMediaPicker` instead of the builder API to remove the `onCreate` restriction entirely (annotated `@ExperimentalMediaKitApi`).

---

## Project Structure

```
MediaKit-android/
├── imagepicker/        ImagePicker, ImageCropProvider interface
├── imagecropper/       CropperView, CropperActivity, MediaKitCropProvider
├── mediapicker/        MediaPicker, unified type + restriction picker
├── imagecompressor/    ImageCompressor, coroutine-based resize/re-encode
├── audiorecorder/      AudioRecorder, RecorderActivity, waveform view
├── videocompressor/    VideoCompressor, MediaCodec-based transcoder
├── mediapreviewer/     MediaPreviewer, fullscreen swipe preview
├── mediakit-core/      Shared utilities (PermissionLauncher, TempFileManager)
├── mediakit-bom/       Bill of Materials
└── sample-app/         Demo Compose app — Image / Media / Compress / Record tabs
```

## Design Goals

- **Modular** — each artifact is independently publishable and usable
- **Lightweight** — no third-party image loading dependencies
- **Lifecycle-safe** — all `registerForActivityResult` calls happen before `onStart`
- **Kotlin-first** — sealed results, fluent builder, coroutine support
- **Extensible** — implement `ImageCropProvider` to plug in any crop library

## Migrating from ArthurHub android-image-cropper

`com.github.ArthurHub:android-image-cropper` is deprecated and unmaintained. MediaKit's `imagecropper` is a drop-in replacement published on Maven Central (no JitPack required).

**Dependency swap:**

```kotlin
// Remove
implementation("com.github.ArthurHub:android-image-cropper:2.8.0")

// Add
implementation("io.github.akshayashokcode:imagepicker:1.0.0")
implementation("io.github.akshayashokcode:imagecropper:1.0.0")
```

**API comparison:**

```kotlin
// ArthurHub — onActivityResult era
CropImage.activity()
    .setCropShape(CropImageView.CropShape.OVAL)
    .setAspectRatio(1, 1)
    .setFixAspectRatio(true)
    .setOutputCompressQuality(90)
    .setMaxResultSize(2048, 2048)
    .start(this)

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
        val result = CropImage.getActivityResult(data)
        if (resultCode == RESULT_OK) { val uri = result.uri }
    }
}
```

```kotlin
// MediaKit — lifecycle-safe, sealed results, no onActivityResult
val picker = ImagePicker.with(this, this)
    .source(MediaSource.Gallery)
    .crop(
        MediaKitCropProvider(
            CropperOptions(
                cropShape = CropShape.Circle,
                aspectRatios = listOf(AspectRatio.Square),
                lockAspectRatio = true,
                outputFormat = OutputFormat.JPEG(quality = 90),
                maxOutputWidth = 2048,
                maxOutputHeight = 2048
            )
        )
    )
    .onResult { result ->
        when (result) {
            is ImagePickerResult.Success -> { val uri = result.uri }
            is ImagePickerResult.Cancelled -> { }
            is ImagePickerResult.Error -> { }
            else -> Unit
        }
    }
```

See [MIGRATION.md](MIGRATION.md) for the full options mapping, FileProvider differences, and standalone `CropperView` migration.

---

## Requirements

- minSdk 24 (Android 7.0+)
- Kotlin

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

MIT — see [LICENSE](LICENSE).
