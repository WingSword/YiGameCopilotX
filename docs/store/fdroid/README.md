# F-Droid inclusion submission

[MR !49945: New app: YiGame Tabletop Companion](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945)
was submitted on 2026-09-24 and is open, pending official build, scanner and
maintainer review. This is not evidence of acceptance or availability in F-Droid.

**2026-09-25:** the maintainer ran official CI. The APK build stopped at source
scanning on two JetBrains Compose development repository declarations. The local
recipe now removes them, and a focused before/after scan reports 2 errors before
and 0 after. Updating the GitLab fork and rerunning official CI remain pending
login. See [current publication status](../PUBLISH_STATUS_20260925.md).

The `fdroid` distribution includes optional network rooms and user-configured AI;
its external APK update shortcut is disabled. The UI is currently Chinese.
The owner authorized official inclusion and selected Apache-2.0. The root
`LICENSE` covers original project code; third-party notices, including the Noto
Sans SC OFL and server QR encoder MIT license, remain in place.

## Source and metadata

- Application ID: `org.walks.gamecopilot`.
- Latest version: 1.6, version code 10, tagged `v1.6`.
- Pinned source: `e5129ab22ed6dc2c7baf7d5fd69225c3557ae4bd` in
  [WingSword/YiGameCopilotX](https://github.com/WingSword/YiGameCopilotX).
- Recipe: `org.walks.gamecopilot.yml`; copied to
  `metadata/org.walks.gamecopilot.yml` in the public fdroiddata fork.
- Fork branch: `ZephyrSword/fdroiddata:codex/org.walks.gamecopilot`, commit
  `f8c86651494083ec70f4a1f702071586476856ef`. The MR adds only that metadata file.
- Build uses `composeApp`, the `fdroid` Gradle flavor and official repositories.
  Version-tag auto-updates are enabled.
- English and Chinese Fastlane descriptions, images and changelogs are in
  `fastlane/metadata/android/`. Optional DeepSeek is disclosed as `NonFreeNet`.

## Validation and next action

`fdroid rewritemeta` and `fdroid lint` pass with fdroidserver 2.4.5 on Ubuntu WSL.
Environment warnings concern missing Linux apksigner and mounted public config
permissions. The APK signatures and alignment were separately checked with
Android SDK tools.

[Upstream CI 35953950348](https://github.com/WingSword/YiGameCopilotX/actions/runs/35953950348)
built all four signed APK distributions plus the Play bundle using JDK 17 and
official repositories. This supports the recipe but is not an official unsigned
F-Droid build/scanner result.

GitLab registration verification is complete. The initial fork pipelines stopped
at a separate CI identity gate. Maintainer linsui subsequently triggered
[official pipeline 2877814081](https://gitlab.com/fdroid/fdroiddata/-/pipelines/2877814081).
Its metadata checks passed, but the build job found two unsupported Maven
repository declarations during source scanning. The revised prebuild recipe
removes the development repository in both Gradle files without disabling
the scanner. The available GitLab browser is currently signed out, so the local
fix has not yet reached the MR. Resume that MR after login and verify the next
official build and APK check; local scanning is not an APK-build result.

## Reproducibility and distribution limits

Bit-for-bit reproducibility against an upstream **fdroid-flavor** APK has not
been established. The current public GitHub APK is the **direct** flavor and is
not a matching binary. Do not set `Binaries` or `AllowedAPKSigningKeys` without
actual reproducibility evidence. Otherwise F-Droid signs its build with its own
key, and it cannot update a differently signed Play/direct installation with
the same application ID.

ABI splitting was evaluated: the 27,592,675-byte test APK contains only 37,392
compressed bytes of native libraries across all four ABIs (about 0.14%), so the
size saving would be negligible.

The optional default room server currently uses HTTP. Do not claim encrypted
transport or apply the domestic offline privacy policy to this distribution.
Production endpoint and full-channel privacy review remain separate open work.

References: [inclusion policy](https://f-droid.org/docs/Inclusion_Policy/),
[submission guide](https://f-droid.org/docs/Submitting_to_F-Droid_Quick_Start_Guide/),
[reproducible builds](https://f-droid.org/docs/Reproducible_Builds/).
