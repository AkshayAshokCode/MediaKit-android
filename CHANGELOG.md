# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

## [1.0.0] — 2026-06-07

### New module — mediakit-core
- `MediaKitException` — abstract base class extended by all module exception types
- `PermissionLauncher` — generic `Array<String>` permission request; replaces imagepicker's camera-only launcher
- `TempFileManager` — parameterised authority, guaranteed cleanup on all code paths
- `AppAvailabilityUtils` — intent resolution helpers
- `@ExperimentalMediaKitApi` annotation for evolving Compose APIs
- Published to Maven Central: `io.github.akshayashokcode:mediakit-core:1.0.0`

### New module — mediapicker
- Unified storage picker for images, videos, audio, and documents via a single fluent API
- `MediaType` sealed class: `Image`, `Video`, `Audio`, `Document`, `All`
- Contract selection: `PickVisualMedia` for image/video combinations (API 33+), `OpenDocument` for audio/document/mixed
- `allowMultiple(true)` for multi-selection
- Post-selection restriction enforcement via `restrictMimeTypes()` and `restrictExtensions()`
- `MediaPickerResult`: `Success(item)`, `MultipleSuccess(items)`, `Cancelled`, `Error`
- `MediaItem` sealed class: `Image`, `Video`, `Audio`, `Document`, `Unknown` — each with `uri` and `mimeType`
- `MediaPickerException`: `AppNotFound`, `InvalidUri`, `RestrictedFile`, `Unknown`
- `rememberMediaPicker` Compose API (`@ExperimentalMediaKitApi`)
- Published to Maven Central: `io.github.akshayashokcode:mediapicker:1.0.0`

### New module — imagecompressor
- Coroutine-based image resize and re-encode — no `ActivityResultCaller` required
- Two-pass `BitmapFactory` decode with `inSampleSize` for memory-safe downsampling
- `CompressionOptions`: `maxWidth`, `maxHeight`, `quality`, `format` (JPEG/PNG/WebP), `maxFileSizeBytes` (iterative quality reduction to 30), `preserveExif`
- Output cached to `cacheDir/mediakit-compressed/` — same input + options returns cached file
- `compressAsync()` callback API (delivers on main thread) and `suspend compress()` API
- `ImageCompressionResult`: `Success(uri, originalSizeBytes, compressedSizeBytes)`, `Cancelled`, `Error`
- `ImageCompressionException`: `InvalidSource`, `DecodingFailed`, `EncodingFailed`, `FileCreationFailed`, `Unknown`
- Published to Maven Central: `io.github.akshayashokcode:imagecompressor:1.0.0`

### New module — audiorecorder
- Launches `RecorderActivity` with scrolling waveform (Canvas amplitude bars) and countdown timer
- `AudioRecorderOptions`: `maxDurationSeconds` (0 = unlimited), `format` (`AAC_M4A`), `showWaveform`
- Automatic `RECORD_AUDIO` permission request before launch
- `AudioRecorderFileProvider` — isolated authority `${applicationId}.mediakit.audiorecorder.fileprovider`
- `AudioRecorderResult`: `Success(uri, durationMs)`, `Cancelled`, `Error`
- Published to Maven Central: `io.github.akshayashokcode:audiorecorder:1.0.0`

### New module — videocompressor
- `MediaExtractor` + `MediaCodec` + `MediaMuxer` pipeline — no `ActivityResultCaller` required
- `VideoCompressionOptions`: `maxWidth`, `maxHeight`, `videoBitrateBps`, `frameRate`
- `onProgress` callback (0–100 percent, delivered on main thread)
- `compressAsync()` returns `Job` — call `cancel()` to abort in-progress compression
- `suspend compress()` API for coroutine callers
- `VideoCompressionResult`: `Success(uri, originalSizeBytes, compressedSizeBytes)`, `Cancelled`, `Error`
- Published to Maven Central: `io.github.akshayashokcode:videocompressor:1.0.0`

### New module — mediapreviewer
- Fullscreen swipe-between-items preview activity for images, video, and audio
- `MediaPreviewItem` sealed class: `Image(uri)`, `Video(uri)`, `Audio(uri)`
- `PreviewOptions`: `showShareButton`, `zoomEnabled`
- Pinch-to-zoom on images via `ZoomableImageView`
- No `ActivityResultCaller` required — launches via `startActivity`
- Published to Maven Central: `io.github.akshayashokcode:mediapreviewer:1.0.0`

### New module — mediakit-bom
- `java-platform` Bill of Materials — import once to align all module versions automatically
- `implementation(platform("io.github.akshayashokcode:mediakit-bom:1.0.0"))` replaces per-module version strings
- Published to Maven Central: `io.github.akshayashokcode:mediakit-bom:1.0.0`

### imagepicker
- `MediaSource.Both` now shows a native `AlertDialog` source chooser instead of silently defaulting to gallery
- Camera temp file is deleted on all error and decode-failure paths (was only deleted on cancellation)
- `rememberImagePicker(source)` Compose API — callable anywhere inside a `@Composable`, no `onCreate`-before-`setContent` restriction (`@ExperimentalMediaKitApi`)
- Refactored to depend on `:mediakit-core`: `PermissionLauncher`, `TempFileManager`, and `AppAvailabilityUtils` moved to core; `ImagePickerException` now extends `MediaKitException`
- Published to Maven Central: `io.github.akshayashokcode:imagepicker:1.0.0`

### imagecropper
- `CropperActivity` now performs a two-pass `BitmapFactory` decode with `inSampleSize` capped to `max(maxOutputDimension, 2048)` — eliminates OOM risk on large images
- Refactored to depend on `:mediakit-core`
- Published to Maven Central: `io.github.akshayashokcode:imagecropper:1.0.0`

### sample-app
- 5-tab bottom navigation: Image, Media, Compress, Record, Preview
- Image tab: pick from gallery → crop via `MediaKitCropProvider` → display cropped result
- Media tab: type chips (Image, Video, Audio, Document, All), multiple toggle, results grid
- Compress tab: image compression sub-flow (original size → compress → size comparison); video compression sub-flow (pick video → progress → output)
- Record tab: launch in-app recorder, display waveform preview and playback
- Preview tab: pick multiple items, launch fullscreen swipe previewer
- Versions bottom sheet: all module versions with Maven Central links
- All picker screens use `rememberImagePicker` / `rememberMediaPicker` Compose API

### documentation
- `MIGRATION.md` — complete ArthurHub `android-image-cropper` → MediaKit migration guide: dependency swap, API comparison, options mapping table, result type migration, FileProvider differences, behavioral differences, standalone `CropperView` migration
- README fully rewritten: "which module do I need?" decision table, BOM installation as recommended option, quick-start snippets for all modules, `ActivityResultCaller` requirement table, Compose API documentation

## [0.2.0] — 2026-05-28

### imagecropper
- `CropperOptions` — configurable per-launch: aspect ratios, crop shape, rotate/flip buttons, output format, max/min output size
- `AspectRatio` — `Free`, `Square`, `Ratio(w,h)` with presets `FourThree`, `SixteenNine`, `ThreeTwo`, `FiveFour`; chip row shown automatically when multiple ratios provided
- `CropShape` — `Rectangle` (default) and `Circle` (circular mask, transparent-corner output)
- `OutputFormat` — `JPEG(quality)`, `PNG`, `WebP(quality)`
- Rotate 90° CW/CCW and flip horizontal/vertical transform buttons (opt-in via `showRotateButtons`, `showFlipButtons`)
- Aspect ratio locking enforced during corner-drag resize in `CropTouchHandler`
- Crop rect bounds constraint fixed: MOVE offsets the whole rect; corner resize clamps each edge independently
- Redesigned `CropperActivity` UI: dark top bar (cancel / title / confirm), aspect ratio chip row, bottom transform toolbar
- Immersive mode: navigation bar hidden with `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`; status bar retained for camera cutout safety
- Display cutout and rounded-corner safe area handled via `fitsSystemWindows` and mandatory gesture insets
- System gesture exclusion rect covers full `CropperView` — crop drag no longer triggers back gesture
- `CropperView.onSizeChanged` re-fits image reactively when insets reshape the container
- Published to Maven Central: `io.github.akshayashokcode:imagecropper:0.2.0`

### imagepicker
- Published to Maven Central: `io.github.akshayashokcode:imagepicker:0.2.0`

## [0.1.0] — 2026-05-28

### imagepicker
- Fluent builder API: `ImagePicker.with(context, caller).source().crop().onResult().onError().launch()`
- Gallery picking via `ActivityResultContracts.GetContent`
- Camera capture via `ActivityResultContracts.TakePicture` with automatic EXIF orientation correction
- `MediaSource.Gallery`, `MediaSource.Camera`, `MediaSource.Both` (Both falls back to Gallery)
- Sealed `ImagePickerResult`: `Success`, `SuccessWithBitmap`, `Cancelled`, `Error`
- Sealed `ImagePickerException`: `PermissionDenied`, `AppNotFound`, `FileCreationFailed`, `InvalidUri`, `DecodingFailed`, `FileDeletionFailed`, `IntentFailed`, `Unknown`
- Automatic CAMERA permission request before camera launch
- `ImageCropProvider` interface — plug in any crop library or use built-in `MediaKitCropProvider`
- `ImagePickerFileProvider` — isolated FileProvider subclass; authority `{applicationId}.imagepicker.provider`
- `<queries>` manifest entry for camera intent visibility on Android 11+
- All `registerForActivityResult` calls happen at construction time (before `onStart`)
- Published to Maven Central: `io.github.akshayashokcode:imagepicker:0.1.0`

### imagecropper
- `CropperView` — custom View with matrix-based image fit, touch-drag/resize crop rect
- `CropOverlayDrawer` — dimmed overlay, border, rule-of-thirds grid, L-shaped corner handles
- `CropTouchHandler` — detects MOVE / corner / edge areas; enforces `MIN_CROP_SIZE`
- `CropperSavedState` — full Parcelable save/restore of crop rect across rotation
- `CropperActivity` — standalone Activity: receives URI via `EXTRA_INPUT_URI`, returns cropped JPEG URI via `EXTRA_OUTPUT_URI`
- `MediaKitCropProvider` — implements `ImageCropProvider`; wires `CropperActivity` into the picker flow
- `getCroppedImage()` — returns cropped `Bitmap` from current rect, clamped to image bounds
- Published to Maven Central: `io.github.akshayashokcode:imagecropper:0.1.0`

### sample-app
- Compose UI: empty state → gallery pick → crop → result display
- Error and exception feedback via Toast
