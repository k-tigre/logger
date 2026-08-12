# logger

Unified logging for Tigre Android/Kotlin projects: multi-backend `Log` facade, Logcat, Firebase Crashlytics breadcrumbs, local SQLite store, JVM console.

Published to [GitHub Packages](https://github.com/k-tigre/logger/packages).

## Modules

| Artifact | Description |
|----------|-------------|
| `logger-core` | KMP metadata — `Log`, `Log.Logger`, `Flow.debugLog` |
| `logger-core-android` | Android implementation (pulled transitively) |
| `logger-core-desktop` | JVM/desktop implementation (pulled transitively) |
| `logger-logcat` | Android Logcat |
| `logger-crashlytics` | Firebase Crashlytics |
| `logger-internal-store` | KMP metadata — SQLDelight DB + `LogsProvider` |
| `logger-internal-store-android` | Android DB driver (pulled transitively) |
| `logger-internal-store-desktop` | JVM DB driver (pulled transitively) |
| `logger-console` | JVM stdout/stderr |
| `logger-debug-ui` | Android Compose debug screen (`DebugActivity`) for browsing `LogsProvider` |

App modules depend only on `logger-core`, `logger-logcat`, etc. Platform artifacts (`*-android`, `*-desktop`) are resolved automatically by Gradle.

**Group:** `com.github.k-tigre`

## Publish (maintainer)

1. Public repo: [github.com/k-tigre/logger](https://github.com/k-tigre/logger)
2. Add to `~/.gradle/gradle.properties` (see `gradle.properties.example`):
   ```properties
   gpr.user=YOUR_GITHUB_USERNAME
   gpr.key=ghp_...   # PAT with write:packages
   ```
3. Tag and push:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
   GitHub Actions runs `publish` and uploads all modules.

Local check without remote:

```bash
./gradlew publishToMavenLocal
```

## Consume in an app

`settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/k-tigre/logger")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                    ?: ""
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GITHUB_TOKEN")
                    ?: ""
            }
        }
        mavenLocal() // optional: after publishToMavenLocal
    }
}
```

`build.gradle.kts` (app module):

```kotlin
val tigreLogger = "1.1.0"

dependencies {
    implementation("com.github.k-tigre:logger-core:$tigreLogger")
    implementation("com.github.k-tigre:logger-logcat:$tigreLogger")
    implementation("com.github.k-tigre:logger-crashlytics:$tigreLogger")
    implementation("com.github.k-tigre:logger-internal-store:$tigreLogger")
    // optional debug UI (declare LAUNCHER / deep-link in the host app if needed)
    debugImplementation("com.github.k-tigre:logger-debug-ui:$tigreLogger")
}
```

### Debug UI

`DebugActivity` is registered in the library manifest **without** a `MAIN`/`LAUNCHER` intent-filter.
Add one in the host app when you want a launcher icon:

```xml
<activity
    android:name="by.tigre.logger.debug.DebugActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

Or subclass and add preset filter tabs:

```kotlin
class AppDebugActivity : DebugActivity() {
    override fun createExtraPages(logsProvider: LogsProvider): List<DebugPage> = listOf(
        filteredLogsPage("Analytics", initialTagFilter = "Analytics", logsProvider),
    )
}
```

For a **public** package repo, reading often works with any valid `read:packages` token; CI should set `GITHUB_TOKEN`.

## Standard initialization

**Android debug:**

```kotlin
import by.tigre.logger.*

Log.init(
    Log.Level.VERBOSE,
    LogcatLogger(),
    CrashlyticsLogger(),
    DbLogger(LogDatabaseDriverFactory.create(this), Process.myPid())
)
```

**Android release:**

```kotlin
Log.init(Log.Level.DEBUG, CrashlyticsLogger())
```

**Desktop:**

```kotlin
Log.init(Log.Level.DEBUG, ConsoleLogger())
```

Feature modules should depend only on `logger-core` for `Log.d { }` calls.
