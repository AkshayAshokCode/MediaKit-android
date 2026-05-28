# Known Improvements

## In Progress / Planned

### MediaSource.Both
`MediaSource.Both` currently falls back to gallery. A bottom sheet chooser
(gallery vs camera) is the intended implementation.

### Compose-native API
The current API requires construction in `Activity.onCreate()` before
`setContent` due to the `registerForActivityResult` lifecycle constraint.
A future Compose-friendly surface (e.g. `rememberImagePicker()`) using
`ActivityResultRegistry` directly would remove this requirement.

### Camera temp-file cleanup
`FileUtils.deleteTempFile` is called on cancellation but not on all error
paths in `CameraImageLauncher`. A systematic cleanup pass is needed.

### Bitmap memory pressure
`CropperActivity` holds the full decoded bitmap in memory. Large images
should be downsampled to the screen resolution before display, with the
original used only for the final crop export.

## Resolved (no longer outstanding)
- ~~API Cleanup~~ — public surface reduced; builder API is stable
- ~~Bitmap Safety~~ — EXIF orientation fixed; stream leaks closed
- ~~Sample App~~ — redesigned with gallery → crop → result flow
- ~~Lifecycle handling~~ — all launchers registered before `onStart`
- ~~FileProvider conflicts~~ — isolated subclasses per module
- ~~Publishing~~ — both modules live on Maven Central at `io.github.akshayashokcode`
- ~~Crop enhancements~~ — aspect ratio lock, circle mask, rotate/flip, output format/size (0.2.0)
- ~~Gesture conflict~~ — system gesture exclusion rects applied to `CropperView` (0.2.0)
- ~~Display cutout~~ — status bar retained; `fitsSystemWindows` handles safe area (0.2.0)

## Future Areas

- Media compression (resize before upload)
- Video picking support
- Media filters / adjustments
- CI/CD pipeline for automated publishing on release tags
