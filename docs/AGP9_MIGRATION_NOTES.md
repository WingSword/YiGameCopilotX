# AGP 9 Migration Notes

## Current module layout

The project now keeps a two-module KMP structure:

- `composeApp` as the Android app entry + cross-platform UI module
- `shared` as the shared capability module

## Key build changes

- AGP: `9.0.1`
- Gradle: `9.3.0-rc-1` (team decision; minimum required is `9.1.0`)
- JVM target: `17`
- Added root task: `runWeb`

## Transitional flags

`gradle.properties` keeps:

- `android.builtInKotlin=false`
- `android.newDsl=false`

These flags are used as migration bridge to reduce risk while preserving existing Kotlin/DSL behavior.
Plan to remove them in a later phase after completing Kotlin and DSL alignment.

## Follow-up recommendations

1. Align all Kotlin plugin versions to AGP built-in Kotlin baseline.
2. Remove transition flags and verify full build.
3. Add CI matrix:
   - `:composeApp:wasmJsBrowserDistribution`
   - `:composeApp:assembleDebug`
   - shared unit tests
