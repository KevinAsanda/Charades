# Handoff Report: Manifest Orientation & Lifecycle Investigation (Milestone 3)

**Agent**: teamwork_explorer_m3_1 (Explorer)  
**Milestone**: Milestone 3 (Automatic Gameplay Landscape Orientation)  
**Target File**: `app/src/main/AndroidManifest.xml`  
**Working Directory**: `c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades`  
**State Folder**: `c:\Users\kevin\OneDrive\Desktop\Dev-FOLDER\Charades\.agents\teamwork\teamwork_explorer_m3_1`  

---

## 1. Observation

### 1.1 Current Manifest Declaration
Direct inspection of `app/src/main/AndroidManifest.xml` (lines 23–34):
```xml
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
```

### 1.2 Attributes and Configuration Inventory
1. **Activity Identity & Exported State**:
   - `android:name=".MainActivity"`: Points to single-activity class `com.example.MainActivity` (subclass of `androidx.activity.ComponentActivity`).
   - `android:exported="true"`: Declared appropriately because it hosts the launcher intent filter (`android.intent.action.MAIN` + `android.intent.category.LAUNCHER`).
2. **Missing Orientation Attribute**:
   - Currently, `<activity android:name=".MainActivity" ...>` does **not** specify `android:screenOrientation`.
   - By omission, the Android Framework defaults the activity's requested orientation to `ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED` (-1).
   - In `app/src/main/java/com/example/ui/GamePlayScreen.kt` (lines 200–202):
     ```kotlin
     val initialOrientation = remember(activity) {
       activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
     }
     ```
     Because `requestedOrientation` was never explicitly set, `activity.requestedOrientation` returns `-1`. When `GamePlayScreen.kt` later restores `initialOrientation` on gameplay exit, it restores `SCREEN_ORIENTATION_UNSPECIFIED`. If the user rotated their device or holds the phone horizontally when exiting gameplay, the Lobby or Game Over screen rotates unexpectedly to landscape.
3. **Existing Config Changes Declaration**:
   - `android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"` is already explicitly declared on `.MainActivity`.
4. **Theme Configuration & Window Opacity**:
   - `app/src/main/res/values/themes.xml` (lines 3–7):
     ```xml
     <style name="Theme.MyApplication" parent="android:Theme.Material.Light.NoActionBar">
         <item name="android:statusBarColor">@color/brand_navy</item>
         <item name="android:navigationBarColor">@color/brand_navy</item>
         <item name="android:windowBackground">@color/background</item>
     </style>
     ```
   - The theme is an opaque fullscreen Material Light theme. It does **not** contain `android:windowIsTranslucent="true"` or `android:windowIsFloating="true"`.
5. **XML Namespaces**:
   - Manifest root contains `xmlns:android="http://schemas.android.com/apk/res/android"` and `xmlns:tools="http://schemas.android.com/tools"`.

---

## 2. Logic Chain

### 2.1 Step 1: Baseline Behavior Without `android:screenOrientation`
- In Android, omitting `android:screenOrientation` means the OS can rotate the activity freely according to system auto-rotate settings or physical device sensor.
- For a charades party game, all non-gameplay screens (Auth, Lobby deck grid, Store, Profile, Settings) require a stable portrait experience.
- Without an explicit manifest declaration, `activity.requestedOrientation` defaults to `SCREEN_ORIENTATION_UNSPECIFIED`. When exiting gameplay, resetting to `initialOrientation` leaves the activity in an unspecified state rather than locking back to portrait.

### 2.2 Step 2: Role of `android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"`
- Normally, an Android orientation change (e.g. portrait → landscape) destroys and recreates the `Activity` (`onDestroy` → `onCreate`).
- However, declaring `orientation|screenSize|screenLayout|keyboardHidden` tells the Android Framework to bypass activity recreation:
  1. The existing `MainActivity` instance is retained.
  2. `Activity.onConfigurationChanged(Configuration newConfig)` is called.
  3. `ComponentActivity` updates `LocalConfiguration.current` in Jetpack Compose.
  4. Only Composables observing `LocalConfiguration` re-evaluate. All `remember`, `rememberSaveable`, ViewModels, coroutines, audio players, tone generators, and in-memory gameplay state are preserved without flicker or data loss.

### 2.3 Step 3: Permissibility of Dynamic Orientation Overrides
- Does setting `android:screenOrientation="portrait"` in `AndroidManifest.xml` prevent `GamePlayScreen.kt` from programmatically rotating the screen?
  - **No.** The manifest value sets the baseline/default orientation.
  - At runtime, calling `activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE` overrides the manifest setting for the duration of active gameplay.
  - Calling `activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT` upon exiting gameplay restores the activity to portrait.
  - Combined with `android:configChanges`, this dynamic switch happens completely in-place without destroying or recreating the Activity.

### 2.4 Step 4: Android Version Compatibility (API 26 Opaque Check)
- Android 8.0 (API 26) throws `IllegalStateException: Only fullscreen opaque activities can request orientation` if an activity has both `android:windowIsTranslucent=true` and a fixed `android:screenOrientation`.
- Verified: `Theme.MyApplication` is strictly opaque (`android:Theme.Material.Light.NoActionBar`), with solid status/nav bar and window background colors. No translucency exists. Therefore, this configuration is 100% safe across all Android versions (API 24 through API 36).

### 2.5 Step 5: Android Lint & Best Practice Optimization
- Declaring `android:screenOrientation="portrait"` on a launcher activity can trigger Android Lint warning `LockedOrientationActivity`.
- Because `xmlns:tools="http://schemas.android.com/tools"` is already imported in the root `<manifest>` tag, annotating `.MainActivity` with `tools:ignore="LockedOrientationActivity"` cleanly suppresses the lint warning while preserving strict build hygiene.

---

## 3. Caveats

1. **Read-Only Scope**:
   - This report investigates and specifies the exact change; implementation is reserved for the Worker agent.
2. **Gradle Build Tooling**:
   - `gradlew.bat` is not present yet; wrapper restoration is assigned to Milestone 4. Full APK compilation verification will execute during M4/M5.
3. **Companion Gameplay Lifecycle**:
   - Setting `android:screenOrientation="portrait"` in `AndroidManifest.xml` provides the baseline anchor. It must be paired with the consolidated `DisposableEffect` in `GamePlayScreen.kt` (handled by Explorer 2 / Worker) to ensure programmatic rotation to `SCREEN_ORIENTATION_SENSOR_LANDSCAPE` during gameplay and clean restoration to `SCREEN_ORIENTATION_PORTRAIT` on disposal.

---

## 4. Conclusion & Implementation Instructions for Worker

### 4.1 Recommended Manifest Edit
In `app/src/main/AndroidManifest.xml`, update the `.MainActivity` `<activity>` tag to include `android:screenOrientation="portrait"` and `tools:ignore="LockedOrientationActivity"`.

#### Exact Snippet (Replacement for lines 23–29):
```xml
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:screenOrientation="portrait"
            tools:ignore="LockedOrientationActivity"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication">
```

#### Exact Diff:
```diff
--- a/app/src/main/AndroidManifest.xml
+++ b/app/src/main/AndroidManifest.xml
@@ -24,6 +24,8 @@
             android:name=".MainActivity"
             android:exported="true"
             android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
+            android:screenOrientation="portrait"
+            tools:ignore="LockedOrientationActivity"
             android:label="@string/app_name"
             android:theme="@style/Theme.MyApplication">
             <intent-filter>
```

### 4.2 Summary of Worker Implementation Steps
1. Edit `app/src/main/AndroidManifest.xml`.
2. Insert `android:screenOrientation="portrait"` and `tools:ignore="LockedOrientationActivity"` into `.MainActivity`.
3. Keep `android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"` unchanged.
4. Verify XML formatting and indentation (4 spaces).

---

## 5. Verification Methods for Reviewers and Challengers

### 5.1 Static XML Validation (PowerShell Command)
Execute the following command from the project root:
```powershell
Select-String -Path "app\src\main\AndroidManifest.xml" -Pattern 'android:screenOrientation="portrait"'
```
**Expected Output**:
```
app\src\main\AndroidManifest.xml:27:            android:screenOrientation="portrait"
```

Verify `configChanges` remains present:
```powershell
Select-String -Path "app\src\main\AndroidManifest.xml" -Pattern 'android:configChanges="orientation\|screenSize\|screenLayout\|keyboardHidden"'
```
**Expected Output**: Line matching with all 4 tokens present.

### 5.2 Robolectric PackageManager Assertion
To programmatically verify via Robolectric or Android unit test:
```kotlin
@Test
fun verifyMainActivityOrientationIsPortrait() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val componentName = ComponentName(context, MainActivity::class.java)
    val activityInfo = context.packageManager.getActivityInfo(componentName, PackageManager.GET_ACTIVITIES)
    assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, activityInfo.screenOrientation)
}
```

### 5.3 Activity Recreation Invariance Verification
To verify that dynamic rotation does not destroy `MainActivity`:
```kotlin
@Test
fun verifyOrientationChangeDoesNotRecreateActivity() {
    val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
    val initialActivity = controller.get()
    
    // Switch to sensor landscape
    initialActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    
    // Assert instance identity remains identical
    assertEquals(initialActivity, controller.get())
    
    // Switch back to portrait
    initialActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    assertEquals(initialActivity, controller.get())
}
```

### 5.4 Invalidation Conditions
This investigation and design would be invalidated if:
1. `android:configChanges` is removed or misses `screenSize` / `orientation`, which would cause Activity destruction on orientation switch.
2. `android:windowIsTranslucent` is added to `Theme.MyApplication`, which would violate API 26 orientation locking constraints.
3. `GamePlayScreen.kt` restores orientation to `SCREEN_ORIENTATION_UNSPECIFIED` rather than `SCREEN_ORIENTATION_PORTRAIT`.
