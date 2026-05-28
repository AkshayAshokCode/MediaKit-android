# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

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
