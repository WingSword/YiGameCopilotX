# F-Droid submission preparation

This is preparation for the official F-Droid repository, not a private repository
and not evidence of acceptance. The `fdroid` APK includes room features; only the
external APK update shortcut is disabled. The UI is currently Chinese.

The owner selected Apache-2.0 on 2026-09-22. The root `LICENSE` covers original
project code. Third-party notices, including the Noto Sans SC OFL in Compose
resources and the server QR encoder MIT license, remain in place. Source/assets
must still pass F-Droid's scanner and license review.

Upstream descriptions and changelogs live under `fastlane/metadata/android/`.
`org.walks.gamecopilot.yml` is candidate packaging metadata, **not submitted**.
It explicitly discloses the optional third-party DeepSeek API. A local signed
APK is only a test artifact; F-Droid builds from public source.

The candidate is pinned to public commit
`43147662bd8a320fea0df7c7fb12f5b33d9436ee` on
[`codex/store-publication-20260922`](https://github.com/WingSword/YiGameCopilotX/tree/codex/store-publication-20260922).
[GitHub Actions run 35742097588](https://github.com/WingSword/YiGameCopilotX/actions/runs/35742097588)
built and verified all four signed APK channels plus the Play bundle on Ubuntu,
using JDK 17 and official repositories. This is upstream CI evidence, not an
official unsigned F-Droid build or scanner result.

Before opening the inclusion merge request:

1. Finish the privacy policy and production endpoint review. Do not claim that
   the current default HTTP endpoint encrypts transport.
2. Assign the final release version/code/changelog and publish a new tag. Existing
   `v1.5` points to older code. Update the pinned metadata after the final source
   passes validation; do not move the existing tag.
3. Test an unsigned `:composeApp:assembleFdroidRelease` with JDK 17+, SDK 36,
   the pinned Gradle wrapper and official Maven repositories, without local
   signing files, local Maven artifacts or the developer's Gradle init scripts.
4. In an fdroiddata checkout, save the candidate as
   `metadata/org.walks.gamecopilot.yml`, run `fdroid lint`, `fdroid rewritemeta`
   and the official build/scan checks, then fix every reported issue.
5. Submit the tested metadata from an authenticated GitLab account. Retain the
   merge-request URL and record the review outcome. Configure tag updates only
   once the release/tag pattern has been verified.

The current GitLab account has completed login but remains on the mandatory
welcome form. Country/region is required and the observed option list lacks
China. Account setup must use truthful information; no inclusion merge request
has been created. Do not treat this as a missing password or repeat login requests.

Do not set `Binaries` or `AllowedAPKSigningKeys` unless reproducibility against
an upstream APK has actually been established. Otherwise F-Droid uses its own
signing key, so users cannot install that build as an update over a differently
signed Play/direct installation with the same application ID.

References: [inclusion policy](https://f-droid.org/docs/Inclusion_Policy/),
[submission guide](https://f-droid.org/docs/Submitting_to_F-Droid_Quick_Start_Guide/).
