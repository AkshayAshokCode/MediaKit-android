# Keep CropperActivity — launched by intent from MediaKitCropProvider
-keep class com.akshayashokcode.imagecropper.CropperActivity { *; }

# Keep MediaKitCropProvider so consumers can instantiate it by name or reflection
-keep class com.akshayashokcode.imagecropper.MediaKitCropProvider { *; }

# Keep CropperView for consumers embedding it directly in XML layouts
-keep class com.akshayashokcode.imagecropper.CropperView { *; }
