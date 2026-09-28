# F-Droid publication follow-up — 2026-09-28

The [original inclusion MR !49945](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945)
is open and unmerged. The official package page still returned 404 when checked.
There is no confirmed official F-Droid publication.

## Maintainer request and submitted change

[linsui requested R8](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3912807277)
on September 27 at 23:47 Shanghai time. The recipe now enables minification and
resource shrinking while retaining application source
`e5129ab22ed6dc2c7baf7d5fd69225c3557ae4bd` (1.6, version code 10).

The revised recipe was published to the existing fork branch in commits
`850869a0231e4ceeb4ca0be9a16fbfcd08881792` and
`fa3e3db2806c4bbd8fd4120750dba0ae09b289c7`. A public API readback verified that
the final remote metadata matches `docs/store/fdroid/org.walks.gamecopilot.yml`.
The MR description now distinguishes the earlier successful official pipeline
from the current revision requiring a rerun.

A [maintainer reply](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3916466266)
reports the result and requests the official rerun.

The application build configuration also enables R8/resource shrinking centrally
for release builds, independent of signing configuration. The accompanying
`composeApp/proguard-rules.pro` preserves:

- Netty's reflectively instantiated NIO server-channel constructor.
- Leak-detector exclusion method names resolved during static initialization.
- Runtime `ChannelHandler.Sharable` annotations on retained handlers.
- `MessageToByteEncoder` generic signatures and hierarchy read during the
  WebSocket HTTP upgrade.

Warnings are suppressed only for optional native OpenSSL, desktop logging/JFR,
and the guarded JVM debugger-management probe. There is no global `dontwarn`,
blanket library keep, disabled source scanner or new scanner exception.

## Validation

- `fdroid rewritemeta` and `fdroid lint` pass with fdroidserver 2.4.5 on WSL.
- The full prebuild recipe, applied after F-Droid signing-config removal to an
  archive of the pinned source, matches the tested Gradle configuration and
  upstream keep rules. Source scan: **0 errors**, with the existing font warning.
- `:composeApp:assembleFdroidRelease` succeeds for that source with Gradle 9.3.0
  and JDK 21.0.11 on Windows. R8 and resource-shrinking tasks ran.
- The current upstream checkout also builds its signed `fdroidRelease` with the
  same keep rules. A first attempt exhausted local native memory; after stopping
  this task's emulator/compiler daemon and limiting build parallelism, it passed
  in 3m 8s. No signing secrets are included in the evidence.
- Unsigned APK: **12,691,280 bytes**, versus **27,646,171 bytes** in the
  unminified control (**54.1% reduction**). SHA-256:
  `a0528b2d7ca4779eca41da77651a0d775f2a6b8ac0619af2efb3b53deb589e7b`.
- Android SDK manifest checks confirm package `org.walks.gamecopilot`, version
  1.6/code 10, target SDK 35, no debuggable flag and no external APK-install
  permission. The unsigned APK passes 16-KiB-compatible zip alignment checks.
- A separately test-signed copy was installed on the API 36 emulator without
  deleting app data. Startup and offline Spy role dealing/reveal work. Three
  successive WebSocket connections each receive the serialized `JOIN_RESPONSE`,
  exchange `HEARTBEAT` messages, and close cleanly with code 1000.
- Temporary diagnostic logging dependencies were removed before the final build.
  The QA port forward and this task's headless emulator were stopped afterwards.

This is targeted R8 compatibility testing, not full gameplay QA or a claim of
bit-for-bit reproducibility against official Linux F-Droid builders.

## Remaining work and limitations

[Fork pipeline 2889431216](https://gitlab.com/ZephyrSword/fdroiddata/-/pipelines/2889431216)
reports failure with **zero jobs**. An official maintainer-triggered rerun is
needed for `fa3e3db2`. [Pipeline 2883311450](https://gitlab.com/fdroid/fdroiddata/-/pipelines/2883311450)
passed all nine jobs for the older `f8d68d20` recipe; it predates this R8 change.

The LAN creation form can remain in its loading state after the server has
started. This was reproduced in both the minified APK and the exact unminified
control, so it is a pre-existing application issue. The transport test connects
directly to the running local server and does not establish that the whole LAN
UI flow is correct. This limitation is disclosed in the MR.

The default optional cloud-room endpoint still uses HTTP. The transport finding
and optional DeepSeek `NonFreeNet` disclosure remain; HTTPS is not claimed fixed.
Google Play and vivo publication were not changed in this follow-up.

Evidence is retained locally under `artifacts/store-publish-20260928/`, including
the final recipe scan, build logs, APK hashes, remote readback, runtime logs,
round-trip result and role-reveal screenshot. These generated artifacts are not
committed to the source repository.
