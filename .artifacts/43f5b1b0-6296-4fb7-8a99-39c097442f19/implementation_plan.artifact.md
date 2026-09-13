# Fix Build Error (25.0.3 JDK Incompatibility)

The project is failing to build because the Gradle version (8.14.5) is incompatible with the JVM version (25.0.3) currently running it. Gradle 8.14.5 supports Java versions up to 24.

Additionally, the `app/build.gradle.kts` contains invalid SDK versions (36) and an unusual `compileSdk` configuration.

## Proposed Changes

### Build Configuration

#### [MODIFY] [gradle-wrapper.properties](file:///D:/meloScan/gradle/wrapper/gradle-wrapper.properties)
- Upgrade Gradle to a version compatible with Java 25. I will try `9.7.1` as it is the latest stable version.

#### [MODIFY] [build.gradle.kts](file:///D:/meloScan/app/build.gradle.kts)
- Fix `compileSdk` to use a stable version (35).
- Fix `targetSdk` to use a stable version (35).
- Fix the syntax for `compileSdk` to avoid the confusing `release(36)` call.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify the build completes.
- Run `gradle_sync` to ensure the IDE is in sync.
