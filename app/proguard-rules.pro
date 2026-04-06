# ProGuard rules for MoneyTrace
-keep class com.moneytrace.data.** { *; }
-keep class com.moneytrace.service.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
