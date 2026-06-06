# Migrating from ArthurHub android-image-cropper

`com.github.ArthurHub:android-image-cropper` is deprecated and unmaintained. This guide shows how to migrate to `io.github.akshayashokcode:imagecropper`.

---

## 1. Swap the dependency

### Before

```kotlin
// settings.gradle.kts — JitPack repo required
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
implementation("com.github.ArthurHub:android-image-cropper:2.8.0")
```

### After

```kotlin
// settings.gradle.kts — Maven Central only (no extra repo needed)
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

// build.gradle.kts
implementation("io.github.akshayashokcode:imagepicker:1.0.0")
implementation("io.github.akshayashokcode:imagecropper:1.0.0")
// or with the BOM:
implementation(platform("io.github.akshayashokcode:mediakit-bom:1.0.0"))
implementation("io.github.akshayashokcode:imagepicker")
implementation("io.github.akshayashokcode:imagecropper")
```

---

## 2. AndroidManifest.xml

### Before — ArthurHub required you to declare its activity

```xml
<activity
    android:name="com.theartofdev.edmodo.cropper.CropImageActivity"
    android:theme="@style/Base.Theme.AppCompat" />
```

### After — MediaKit auto-registers its activity via manifest merger

No changes needed. `CropperActivity` is declared inside the library's manifest and merged automatically.

---

## 3. Launching the crop flow

### Before — launch from `onActivityResult` era

```kotlin
// In Activity.onCreate or wherever you trigger the pick
CropImage.activity()                                        // no source URI — ArthurHub opens its own picker
    .setCropShape(CropImageView.CropShape.RECTANGLE)
    .setAspectRatio(16, 9)
    .setFixAspectRatio(true)
    .setGuidelines(CropImageView.Guidelines.ON)
    .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
    .setOutputCompressQuality(90)
    .setMaxResultSize(2048, 2048)
    .start(this)

// Receive result in the deprecated callback
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
        val result = CropImage.getActivityResult(data)
        if (resultCode == RESULT_OK) {
            val croppedUri: Uri = result.uri
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            val error: Exception = result.error
        }
    }
}
```

### After — lifecycle-safe builder, no `onActivityResult`

```kotlin
// In Activity.onCreate, before setContent
val picker = ImagePicker.with(this, this)
    .source(MediaSource.Gallery)                            // Gallery / Camera / Both
    .crop(
        MediaKitCropProvider(
            CropperOptions(
                aspectRatios = listOf(AspectRatio.SixteenNine),
                lockAspectRatio = true,
                showRotateButtons = true,
                outputFormat = OutputFormat.JPEG(quality = 90),
                maxOutputWidth = 2048,
                maxOutputHeight = 2048
            )
        )
    )
    .onResult { result ->
        when (result) {
            is ImagePickerResult.Success -> {
                val croppedUri: Uri = result.uri
            }
            is ImagePickerResult.Cancelled -> { }
            is ImagePickerResult.Error -> { }
            else -> Unit
        }
    }

setContent { MyTheme { MyScreen(onPick = { picker.launch() }) } }
```

---

## 4. Options mapping

| ArthurHub | MediaKit |
|---|---|
| `.setCropShape(CropImageView.CropShape.RECTANGLE)` | `cropShape = CropShape.Rectangle` (default) |
| `.setCropShape(CropImageView.CropShape.OVAL)` | `cropShape = CropShape.Circle` |
| `.setAspectRatio(1, 1)` | `aspectRatios = listOf(AspectRatio.Square)` |
| `.setAspectRatio(16, 9)` | `aspectRatios = listOf(AspectRatio.SixteenNine)` |
| `.setAspectRatio(w, h)` | `aspectRatios = listOf(AspectRatio.Ratio(w, h))` |
| `.setFixAspectRatio(true)` | `lockAspectRatio = true` |
| `.setGuidelines(CropImageView.Guidelines.ON)` | Always shown (not configurable) |
| `.setOutputCompressFormat(Bitmap.CompressFormat.JPEG)` | `outputFormat = OutputFormat.JPEG()` (default) |
| `.setOutputCompressFormat(Bitmap.CompressFormat.PNG)` | `outputFormat = OutputFormat.PNG` |
| `.setOutputCompressFormat(Bitmap.CompressFormat.WEBP)` | `outputFormat = OutputFormat.WebP()` |
| `.setOutputCompressQuality(90)` | `outputFormat = OutputFormat.JPEG(quality = 90)` |
| `.setMaxResultSize(w, h)` | `maxOutputWidth = w, maxOutputHeight = h` |
| `.setMinCropResultSize(w, h)` | `minOutputWidth = w, minOutputHeight = h` |
| `.setMultiTouchEnabled(true)` | Always enabled (not configurable) |
| `.setInitialCropWindowPaddingRatio(0.1f)` | Not configurable |
| Rotate buttons | `showRotateButtons = true` |
| Flip buttons | `showFlipButtons = true` |
| Multiple aspect ratio choices | `aspectRatios = listOf(AspectRatio.Free, AspectRatio.Square, AspectRatio.SixteenNine)` |

---

## 5. Result type migration

### Before

```kotlin
// Result delivered via deprecated onActivityResult
val result = CropImage.getActivityResult(data)
val croppedUri: Uri = result.uri        // nullable — crashes if activity result failed silently
val error: Exception = result.error
```

### After

```kotlin
// Sealed class — exhaustive, no nulls, no request codes
when (result) {
    is ImagePickerResult.Success -> { val uri: Uri = result.uri }
    is ImagePickerResult.Cancelled -> { /* user pressed back */ }
    is ImagePickerResult.Error -> { /* result.message describes the failure */ }
    else -> Unit
}
```

---

## 6. FileProvider

### Before — ArthurHub required a manual FileProvider entry

```xml
<!-- In res/xml/file_paths.xml -->
<paths>
    <external-path name="crop_cache" path="Android/data/com.your.app/files/Pictures" />
</paths>

<!-- In AndroidManifest.xml -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### After — MediaKit uses its own scoped authority, no manual setup

The `imagepicker` library ships its own `FileProvider` subclass with authority `${applicationId}.imagepicker.provider` and its own `file_paths.xml`. Nothing needs to be added to your manifest or res directory.

If your app already declares a `FileProvider` with authority `${applicationId}.provider`, there is no conflict — MediaKit's authority is separate.

---

## 7. Behavioral differences

| Behaviour | ArthurHub | MediaKit |
|---|---|---|
| Activity Result API | `onActivityResult` (deprecated) | `ActivityResultContract` (modern) |
| Lifecycle safety | Not guaranteed — launcher registered at any time | Guaranteed — all launchers registered before `onStart` |
| Crop + pick flow | Single activity handles both pick and crop | Pick and crop are separate, composable steps |
| Source picker | Built-in gallery picker inside the library | Delegates to the system picker via `imagepicker` |
| Camera support | Via its own picker | Via `MediaSource.Camera` in `imagepicker` |
| Compose support | None | `rememberImagePicker` for pick-only; builder API for pick+crop |
| Null result safety | URI is nullable — must null-check manually | Sealed result — no nulls |
| JitPack dependency | Required | Not required — published on Maven Central |
| Maintenance status | Archived, no updates since 2022 | Actively maintained |

---

## 8. Standalone CropperView (advanced)

If you were using ArthurHub's `CropImageView` embedded in your own layout, MediaKit has an equivalent:

### Before

```xml
<com.theartofdev.edmodo.cropper.CropImageView
    android:id="@+id/cropImageView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:cropShape="rectangle"
    app:cropAspectRatioX="1"
    app:cropAspectRatioY="1"
    app:cropFixAspectRatio="true" />
```

```kotlin
cropImageView.setImageUriAsync(uri)
val croppedBitmap: Bitmap? = cropImageView.croppedImage
```

### After

```xml
<com.akshayashokcode.imagecropper.CropperView
    android:id="@+id/cropperView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

```kotlin
cropperView.setImageBitmap(bitmap)
val croppedBitmap: Bitmap? = cropperView.getCroppedImage()
```

> Options (aspect ratio, shape, etc.) are configured on `CropperOptions` passed to `CropperActivity`, not as XML attributes on `CropperView`.
