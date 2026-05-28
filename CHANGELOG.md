# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

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
