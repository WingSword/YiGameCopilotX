# Publication follow-up — 2026-09-30

## F-Droid maintainer feedback and submitted revision

[MR !49945](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945) remains
open. The official package page returned HTTP 404 at the beginning of this run;
there is no confirmed installable official F-Droid version.

On September 29 at 19:36 Shanghai time, linsui requested that the
[ProGuard rules](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3923168544)
and [other build fixes](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3923171498)
live upstream, leaving only Maven repository cleanup in the recipe.

The original fork branch now contains recipe commit
`737a28b9ab8a45dd5417b07716de3398ff51fcea`. It pins upstream
`1ed22875bfeb8b603ff1fcbfe545239b6f762138`, tagged **v1.6.1**, version code **11**.
The upstream release configuration contains R8/resource shrinking, the scoped
Netty ProGuard rules, and official stable Gradle 9.3.0 with its distribution
checksum. Only two Maven-repository removal commands remain in prebuild.
Public API readback confirms the submitted recipe matches the local YAML.
The MR description was updated and a
[review reply](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3925635681)
was posted at 02:24 Shanghai time with the upstream fixes, validation and rerun
request. This addresses new actionable feedback, not an unchanged-state nudge.

The previous official pipeline
[2889952044](https://gitlab.com/fdroid/fdroiddata/-/pipelines/2889952044) passed
all nine jobs for the old 1.6 recipe `fa3e3db2`. It does not validate the new head.
[Fork pipeline 2894481258](https://gitlab.com/ZephyrSword/fdroiddata/-/pipelines/2894481258)
failed with zero jobs. A maintainer-triggered official rerun is required.

## Application fixes and verification

Two Android LAN defects were reproduced and fixed upstream:

- The Ktor server inherited the short-lived startup coroutine's job. Although
  the socket started listening, `start()` could not return until the server
  stopped, leaving the create-room form loading. The server and heartbeat now
  use an owned job, cancelled on stop or startup failure.
- Broadcasting reused a single frame whose buffer can be consumed during send.
  Multiple clients could receive an empty message. Each recipient now gets its
  own frame.

The JVM regression harness in `scripts/test-lan-host.py` reproduces the old
startup timeout and empty broadcast. After the fixes it passes startup return,
two-client broadcast contents, welcome/heartbeat, disconnect, stop/restart and
occupied-port failure followed by retry. The test ignores the model's dynamic
default timestamp when comparing payloads.

The minified Android F-Droid release builds successfully. Signature, package,
release manifest and 1.6.1/code 11 checks pass. Local signed APK: 12,703,312 bytes,
SHA-256 `5305f0a1095e847f352e9c678aff21f5c190fd3df629276f8da5bff929d2d0ad`.
This is a Windows/JDK 21 Android release build, not an official Linux unsigned
F-Droid build or a reproducibility claim.

On API 36, a test-signed copy preserves existing app data and passes:

- No-Wi-Fi creation fails with a visible error and restores the create button.
- After connecting the emulator's Wi-Fi, creation enters the room page.
- Three scripted WebSocket peers join and become ready. Starting a four-player
  Spy game sends START_GAME to all three peers and displays the host's role.
- Ending the round returns the room to preparation. Closing it returns to the
  creation form. The crash buffer is empty. Temporary peers, port forwarding
  and this task's emulator were stopped afterwards.

The scripted peers recorded all three successful start receipts, but their
process later reported a socket-close error during teardown with the port
forward/emulator being stopped. This run does not establish clean close
handshakes for every emulator peer; normal close/restart is covered separately
by the JVM regression harness.

These checks cover the affected LAN path, not every game or multiple physical
devices. Harmony uses a separate ArkTS TCP/WebSocket server whose startup returns
after listening; the Kotlin coroutine fix is Android-specific. No new Harmony
device test or Harmony release is claimed.

`fdroid rewritemeta` and `fdroid lint` pass. Scanning an archive of the exact
pinned commit after signing-config removal and the two prebuild commands reports
0 errors, retaining the existing font warning. Four-channel AI privacy tests and
the existing cross-platform checks pass. The app version now includes the prior
full privacy policy and explicit AI opt-in changes; domestic/full flavor
restrictions remain unchanged.

Source commits and tag were pushed to the existing publication branch.
[GitHub run 36610289713](https://github.com/WingSword/YiGameCopilotX/actions/runs/36610289713)
completed successfully: all four signed-channel jobs passed, including the
Google Play bundle job. [GitHub release v1.6.1](https://github.com/WingSword/YiGameCopilotX/releases/tag/v1.6.1)
is public and contains the direct-channel APK. This is upstream distribution,
not an F-Droid/Google Play/vivo store release.

Evidence is in `artifacts/store-publish-20260930/`, including source-scan JSON,
APK verification, regression logs, UI dumps, screenshots and peer receipts.

## Google Play and vivo

The live [Google Play app dashboard](https://play.google.com/console/u/0/developers/5275652110070665181/app/4975340793676853600/app-dashboard)
now shows a separate **桌游助手** application. This supersedes the September 27
note that its creation form was unsubmitted. Application setup initially showed
0 of 11 tasks complete. The full privacy policy URL was saved, and the console
confirmed the change is saved but needs submission from Publishing overview.
No release or complete application review submission occurred in this run.

The live console requires application setup, a closed test with at least 12
opted-in testers for 14 days, then production-access approval. It currently
reports zero opted-in testers. Store details and truthful declarations remain
to be completed. The historical production HTTPS/log/backup facts remain
unverified; they were not filled with invented values.
The owner has been asked to prepare willing testers' Google account addresses
or an existing Google group. No tester invitation was sent.

The vivo domestic draft URL redirected to login. Its private review state and
previously missing contact verification/undertaking/copyright material cannot
be reverified without a valid session. No duplicate draft or changed legal
declaration was submitted.

The recurring six-hour follow-up remains active. Next checks must use the new
recipe/source and new GitHub run, read fresh maintainer feedback, and distinguish
passing CI, merge and official repository availability. Do not repeat unchanged
rerun requests or login/HTTPS reminders.

## Morning follow-up — 07:24–08:10 Shanghai

The public MR API still reports `opened`, recipe head `737a28b9`, 13 user notes,
and last update `2026-09-29T18:24:24.779Z`, matching the previous review reply.
The pipeline list contains no new official run for this head. The old official
run remains green; the current fork run remains failed. The official package
page again returns HTTP 404, so no official installable release is confirmed.
No repeated review comment or rerun request was posted.

Google Play application setup advanced from **1/11 to 6/11**, verified on the
existing application's live dashboard. Saved declarations and settings:

- No advertisements, no advertising ID. Dependency/source/manifest checks
  found no advertising SDK or advertising-ID permission/API. Advertising ID is
  a separate content declaration and is not one of the dashboard's eleven tasks.
- Not developed by or on behalf of a government.
- No financial products/services; the ledger handles virtual game points only.
- No health-related functionality.
- Application category **Entertainment**, with the already authorized public
  support address `YvesSword@outlook.com`. No personal phone number was published.

Each declaration displayed a saved confirmation. The contact form required one
retry before its saved confirmation appeared. The dashboard subsequently marked
category/contact setup complete. These are saved changes, not a submitted or
approved app release.

The Chinese title, short description and full description from
`fastlane/metadata/android/zh-CN/` were saved as the default store-listing draft.
After leaving and reopening it, the console still showed the draft and all
three text values. Images remain absent: the resource sidebar opened, but its
Upload action did not expose a usable file picker in this browser session or a
native file-dialog window. No assets were uploaded or substituted. Prepared
images remain available locally for continuation.

Five dashboard tasks remain: app access, content rating, target audience, data
safety, and completing the store listing. The 12-person/14-day closed test still
has zero opted-in testers. No new tester request was sent, no AI credentials
were invented, and the unresolved HTTPS/log/backup facts were not declared as
verified. No bundle upload or release review submission occurred in this run.

The vivo domestic draft still redirects to the official login page. No private
review state could be read. The existing login/material requirements have not
been repeated to the owner.

Evidence: `artifacts/store-publish-20260930/mr-morning.json`,
`pipelines-morning.json`, `fdroid-package-morning.html`,
`play-declaration-saves.json`, `play-dashboard-morning.txt`,
`play-dashboard-6of11.png`, `play-listing-draft-morning.txt`, and
`vivo-morning.txt`. The original MR and Play draft tabs are retained for the next
follow-up. No new review result or owner action requires an immediate notification.

## Afternoon follow-up — 13:24–14:01 Shanghai

F-Droid is unchanged: MR `opened`, head `737a28b9`, 13 user notes, last update
`2026-09-29T18:24:24.779Z`. No new official pipeline appears in the MR pipeline
list, and the package page returns HTTP 404. No duplicate comment was posted.
Evidence: `mr-afternoon.json`, `pipelines-afternoon.json`, and
`fdroid-package-afternoon.html` in the September 30 artifact directory.

Google Play now confirms **7/11 setup tasks complete**. The Chinese default
listing is saved with status **ready to send for review** (`可以送审`): title,
short/full description, 512×512 icon, 1024×500 feature graphic, and all three
1080×1920 phone screenshots. This supersedes the morning image-upload blocker.
The supported file-chooser event flow successfully uploaded the existing local
assets; no native-dialog workaround or new asset generation was needed. The
feature graphic created with assistant-written SVG was marked AI-generated or
edited in the per-asset declaration. Actual screenshots were not marked as
synthetic images. Screenshot order currently shown by the console is 3, 1, 2.
No English locale was added in this run.

The listing is prepared for review, not submitted/approved/published. No AAB was
uploaded and no app release or closed test was started. Four setup tasks remain:
app access, content rating, target audience, data safety. The dashboard still
shows zero opted-in testers and the 12-person/14-day requirement.

New concrete owner actions were requested once:

- The content-rating entry page states that completing the questionnaire means
  accepting [IARC Terms of Use](https://web.iarcservices.com/terms). Owner consent
  is pending. No questionnaire was started or rating submitted.
- The live app-access dialog requires full feature access, in English, and says
  reviewers will not create an account, use their own account, buy access or
  start a trial. Optional DeepSeek needs a valid dedicated review key, which is
  not available. The owner was asked to prepare one and enter it only in the
  official Console, not in chat or the repository. No credential was invented,
  read from unrelated stores, or committed. The full-access checkbox was not
  checked; the exploratory unsaved selection was discarded and re-read as blank.

English review instructions and source-backed rating evidence are prepared in
`google-play/REVIEW_PREPARATION.md`. The evidence includes Avalon assassination
text, witch-hunt death results, the Drunk role, wine words, and participant
drawings/guesses. These are notes for the actual questionnaire, not a completed
content audit or a guessed age rating.

Browser reconnection was needed after a timeout while discarding the unsaved
access form. The recovered page confirmed no access declaration had been saved.
The IARC entry page is retained for follow-up. Evidence includes
`play-access-requirements.txt`, `play-access-detail-form.txt`,
`play-iarc-required.txt`, `play-iarc-consent-required.png`,
`play-dashboard-afternoon.txt`, and `play-dashboard-7of11.png`.
The existing HTTPS/log-retention, tester, and vivo login/material dependencies
were not replaced with assumed facts or repeated requests.

## Evening follow-up — 19:25–19:33 Shanghai

Fresh public API responses and the signed-in original MR discussion agree:
MR `opened`, head `737a28b9`, 13 user notes, last update
`2026-09-29T18:24:24.779Z`. The latest comment remains our September 30 02:24
reply; no new maintainer request needs a response. No comment or rerun request
was repeated. The pipeline list still has no official run for the current head:
fork run `2894481258` is failed, while official run `2889952044` passed for the
older `fa3e3db2` recipe. The official package page again returns HTTP 404; no
official installable version is confirmed.

Google Play's live dashboard still shows **7/11** setup tasks complete. The
pending IARC agreement and dedicated DeepSeek reviewer credential have not
received owner answers. Tester and server facts also remain unresolved. No
agreement was accepted and no content declaration, bundle, review or release
was submitted. The vivo domestic draft again redirects to the official account
login page, so its private review state cannot be verified. The existing owner
requests were not repeated.

Evidence: `mr-evening.json`, `pipelines-evening.json`,
`fdroid-package-evening.html`, `mr-comments-evening.txt`, and
`other-stores-evening.json` under `artifacts/store-publish-20260930/`.
Play's normal DOM snapshot timed out after navigation; the supported visible
DOM reader recovered the dashboard's explicit seven-of-eleven progress button.
The MR and Play tabs are retained for continuation. No new actionable review
feedback or material publication change requires a notification this time.
