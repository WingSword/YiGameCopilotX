# F-Droid submission preparation

This is preparation for the official F-Droid repository, not a private repository
and not evidence of acceptance. The `fdroid` APK includes room features; only the
external APK update shortcut is disabled. The UI is currently Chinese.

The owner selected Apache-2.0 on 2026-09-22. The root `LICENSE` covers original
project code. Third-party notices, including the Noto Sans SC OFL in Compose
resources and the server QR encoder MIT license, remain in place. Source/assets
must still pass F-Droid's scanner and license review.

Upstream descriptions and changelogs live under `fastlane/metadata/android/`.
`org.walks.gamecopilot.yml.in` is a packaging template, **not submitted metadata**.
It explicitly discloses the optional third-party DeepSeek API. A local signed
APK is only a test artifact; F-Droid builds from public source.

Before opening the inclusion merge request:

1. Finish the privacy policy and production endpoint review. Do not claim that
   the current default HTTP endpoint encrypts transport.
2. Publish the tested source and license at a new release commit/tag. Existing
   `v1.5` does not contain these local changes. Update version/code/changelog if
   required by the release, and pin the exact commit in the template.
3. Test an unsigned `:composeApp:assembleFdroidRelease` with JDK 17+, SDK 36,
   the pinned Gradle wrapper and official Maven repositories, without local
   signing files, local Maven artifacts or the developer's Gradle init scripts.
4. In an fdroiddata checkout, save the filled template as
   `metadata/org.walks.gamecopilot.yml`, run `fdroid lint`, `fdroid rewritemeta`
   and the official build/scan checks, then fix every reported issue.
5. Submit the tested metadata from an authenticated GitLab account. Retain the
   merge-request URL and record the review outcome. Configure tag updates only
   once the release/tag pattern has been verified.

Do not set `Binaries` or `AllowedAPKSigningKeys` unless reproducibility against
an upstream APK has actually been established. Otherwise F-Droid uses its own
signing key, so users cannot install that build as an update over a differently
signed Play/direct installation with the same application ID.

References: [inclusion policy](https://f-droid.org/docs/Inclusion_Policy/),
[submission guide](https://f-droid.org/docs/Submitting_to_F-Droid_Quick_Start_Guide/).
