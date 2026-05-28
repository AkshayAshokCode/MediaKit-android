# Keep CropperActivity — launched by intent from MediaKitCropProvider
-keep class com.akshayashokcode.imagecropper.CropperActivity { *; }

# Keep MediaKitCropProvider so consumers can instantiate it
-keep class com.akshayashokcode.imagecropper.MediaKitCropProvider { *; }

# Keep CropperView for consumers embedding it directly in XML layouts
-keep class com.akshayashokcode.imagecropper.CropperView { *; }

# Keep public API — options, shape, format types passed by consumers
-keep class com.akshayashokcode.imagecropper.CropperOptions { *; }
-keep class com.akshayashokcode.imagecropper.AspectRatio { *; }
-keep class com.akshayashokcode.imagecropper.AspectRatio$* { *; }
-keep class com.akshayashokcode.imagecropper.CropShape { *; }
-keep class com.akshayashokcode.imagecropper.CropShape$* { *; }
-keep class com.akshayashokcode.imagecropper.OutputFormat { *; }
-keep class com.akshayashokcode.imagecropper.OutputFormat$* { *; }
