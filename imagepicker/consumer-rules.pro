# Keep sealed result and exception types used in consumer when-expressions
-keep class com.akshayashokcode.imagepicker.model.** { *; }

# Keep the crop provider interface so custom implementations survive shrinking
-keep interface com.akshayashokcode.imagepicker.crop.** { *; }
-keep class com.akshayashokcode.imagepicker.crop.** { *; }
