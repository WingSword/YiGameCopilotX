# F-Droid inclusion submission

[MR !49945: New app: YiGame Tabletop Companion](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945)
was submitted on 2026-09-24 and is open, pending maintainer review and merge.
This is not evidence of acceptance or availability in F-Droid.

**2026-09-29 check:** [official pipeline 2889952044](https://gitlab.com/fdroid/fdroiddata/-/pipelines/2889952044)
passed all nine jobs for the current R8 recipe `fa3e3db2`, including APK build
and APK checks, on September 28 at 22:47 Shanghai time. The requested R8 rerun
is complete. The MR remains open and the package page returns 404; review,
merge and repository publication remain pending. A six-hour recurring follow-up
is now active in the owner's task. See
[current publication status](../PUBLISH_STATUS_20260929.md).

**2026-09-28 follow-up:** maintainer linsui requested R8. The original MR now
enables code/resource shrinking and includes the Netty reflection, handler
annotation and encoder signature rules verified on Android. Recipe head is
`fa3e3db2806c4bbd8fd4120750dba0ae09b289c7`. Local metadata checks, source scanning
(0 errors), an unsigned build of the exact pinned release, and three successive
LAN WebSocket round trips pass. The APK is 12,691,280 bytes (54.1% smaller).
The latest fork pipeline has zero jobs, so a fresh official CI rerun and review
are still needed. The earlier nine-job success predates R8. See
[current publication status](../PUBLISH_STATUS_20260928.md) for evidence and
the pre-existing LAN creation-screen limitation reproduced in the control.

**2026-09-27 check:** [official pipeline 2883311450](https://gitlab.com/fdroid/fdroiddata/-/pipelines/2883311450)
passed all nine jobs, including APK build and APK checks, on September 26 at
01:07 Shanghai time. The MR is still open and the official package page returns
404. The report contains 13 findings, including a major cleartext-traffic
warning, minor INTERNET permission/no-R8 notices and informational metadata.
The default HTTP room transport has not been fixed by the build-recipe changes.
See [current publication status](../PUBLISH_STATUS_20260927.md).

**2026-09-25 follow-up:** the maintainer reran official CI and the repository
scanner fix passed. The build then stopped because `gradlew-fdroid` excludes
release candidates such as the source's Gradle 9.3.0-rc-1 from its supported
checksum map. The recipe now selects stable 9.3.0; this fix is in the original MR.
A local unsigned release build from the exact pinned source succeeds. The new
fork pipeline remains blocked before job creation by GitLab CI identity
verification; an official rerun is requested. See
[current publication status](../PUBLISH_STATUS_20260925.md).

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
  `fa3e3db2806c4bbd8fd4120750dba0ae09b289c7`. The MR adds only that metadata file.
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
the scanner. The fix was committed to the existing fork branch after login.
[Fork pipeline 2881427691](https://gitlab.com/ZephyrSword/fdroiddata/-/pipelines/2881427691)
has zero jobs and requests separate GitLab CI identity verification. A
[maintainer reply](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3902520403)
provided the source-scan validation and requested an official rerun.

The maintainer reran [official pipeline 2881660648](https://gitlab.com/fdroid/fdroiddata/-/pipelines/2881660648)
on September 25 at 16:54 Shanghai time. All seven metadata/source-check jobs
passed, and source scanning inside the build job also passed. The
[build log](https://gitlab.com/fdroid/fdroiddata/-/jobs/16730083254#L244) then reports
`No hash for gradle version 9.3.0-rc-1`. The public `gradlew-fdroid` checksum URL
matcher accepts numeric stable versions only. The recipe adds one prebuild line
replacing this release candidate with stable Gradle 9.3.0, retaining the same
application commit and features. No checksum verification or scanner is bypassed.

Validation of this revision:

- `rewritemeta` and `lint` pass; exact-release source scanning remains at 0 errors
  with the existing font warning.
- Gradle 9.3.0's downloaded distribution matches both its official checksum and
  the F-Droid transparency log:
  `0d585f69da091fc5b2beced877feab55a3064d43b8a1d46aeb07996b0915e0e0`.
- An archive of the pinned source, processed with the revised prebuild commands
  and `fdroidserver.common.remove_signing_keys`, builds successfully using
  `:composeApp:assembleFdroidRelease` on Windows with JDK 21.0.11 and Gradle 9.3.0.
  All 77 tasks executed. This is supporting local evidence, not official Linux CI.
- The unsigned APK's package, version 1.6/code 10, fdroid channel, non-debuggable
  status and absence of external APK-install permission pass manifest checks.
  APK SHA-256: `eabb0c218e7aaf26ff94083c68e69cc72ee8d14c42bdd792fcdf022e421a83ae`.
- Evidence is under `artifacts/store-publish-20260925/gradle-stable-fix/`.

The new [fork pipeline 2881810096](https://gitlab.com/ZephyrSword/fdroiddata/-/pipelines/2881810096)
has 0 jobs and explicitly requests separate GitLab CI identity verification.
At that point, an official rerun for `f8d68d20`, APK checks and review were pending.
The [maintainer reply](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3903149599)
reports this fix and the local unsigned build and requests that rerun.

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
