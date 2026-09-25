# Store publication status — 2026-09-25

Neither F-Droid nor Google Play is published yet. This update supersedes their
status in the September 24 snapshot. No vivo form or server deployment was
changed during this check.

## F-Droid

- [MR !49945](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945) remains
  open and unmerged; the official package page returns HTTP 404.
- Maintainer linsui triggered official
  [pipeline 2877814081](https://gitlab.com/fdroid/fdroiddata/-/pipelines/2877814081)
  on September 24 and requested that the submitter check it. The MR is labelled
  `waiting-on-response`. The previous account-level CI gate is no longer the
  immediate cause of this pipeline's failure.
- Schema validation, lint, rewritemeta, checkupdates and the source-check job
  passed. The build job
  [16701691193](https://gitlab.com/fdroid/fdroiddata/-/jobs/16701691193#L228)
  stopped during source scanning because the JetBrains Compose development
  Maven repository occurs in both `settings.gradle.kts` and
  `composeApp/build.gradle.kts`. No APK was built; the APK-check job was skipped.
- The local recipe now removes these two repository declarations during
  `prebuild`, while retaining the same source commit and application features.
  No scanignore or scanner bypass was added.
- Local `fdroid rewritemeta` and `fdroid lint` pass. With fdroidserver 2.4.5,
  the exact tagged source and freshly downloaded official SUSS signatures,
  a before/after source scan reproduces 2 baseline errors and reports 0 errors
  with the revised recipe. An existing font-file warning remains; no APK build
  or reproducibility result is claimed. Evidence is under
  `artifacts/store-publish-20260925/`.
- The initial WSL CLI scan could not clone GitHub because its direct network
  connection timed out. The focused scan above instead used `git archive` of
  the exact local public release commit. It did not scan an arbitrary modified
  working tree or disable signature checks.
- After the owner signed in, the fix was committed to the existing GitLab fork
  branch as `56d5c214986f616cf7901150356dfa49c77ff032`. The remote diff contains
  exactly the two intended added lines in the metadata file; no second MR was
  opened and the pinned application source is unchanged.
- The new fork [pipeline 2881427691](https://gitlab.com/ZephyrSword/fdroiddata/-/pipelines/2881427691)
  stopped before creating any jobs. Its page explicitly requires the separate
  GitLab CI identity verification; this is not a new source/build failure.
- [A maintainer reply](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3902520403)
  reports the fix, local validation and remaining font warning, and requests a
  rerun in the official F-Droid project. An official build and APK check for the
  updated commit, maintainer approval and publication remain pending.

## Default room server

- The owner reiterated that the friend's original Aliyun server must remain in
  use. No server address was changed.
- Android/KMP `CloudRoomClient.DEFAULT_SERVER` and Harmony
  `CloudRoomStore.DEFAULT_SERVER` both remain `http://8.133.216.39:8080`.
- The actual Google Play 1.6/code 10 AAB contains this endpoint in
  `base/dex/classes4.dex`. Its SHA-256 remains
  `fb2c56a57ad211cecf853b2529d43d8264ed18f42028c83539c5e133a52a7f7e`.
- A fresh read-only request to that server's `/health` returned HTTP 200 and
  `{"status":"ok","protocol":1}`. This confirms reachability and the health
  endpoint only, not all game flows or the deployed server revision.
- Existing user-selected custom servers/sessions are retained by design; the
  compiled default does not override those settings.

## Google Play

- The owner can now enter the personal developer account named `风落`. There
  were no existing apps in the account. No further registration payment is
  needed merely to enter this account.
- The create-app page is filled with `桌游助手`, `org.walks.gamecopilot`, Simplified
  Chinese, application type and free pricing. Play confirms that the package
  name is available. This is an unsubmitted form, not a created app listing.
- No AAB was uploaded, no release was submitted, and no signing key was exported.
  The create form requires policy, Play App Signing terms and export declarations;
  none were accepted on the owner's behalf in this turn.
- Full-channel privacy text and an offline-readable in-app entry are now
  implemented for Android/KMP and Harmony, including the Web room entry. The
  public HTML is generated from the same text. The domestic policy is unchanged.
  The text explicitly identifies HTTP transport and unknown host log/backup
  retention; neither is presented as resolved.
- [The full policy is public](https://wingsword.github.io/YiGameCopilotX/store/privacy-full.html).
  GitHub Pages built commit `cd649b0`; a fresh HTTPS response returned 200 and
  matched the local generated HTML exactly. This policy publication is not a
  new APK/AAB release. Harmony changes are saved in the sibling project and
  archived under `artifacts/store-publish-20260925/privacy-harmony/`.
- Optional DeepSeek now requires explicit consent before sending context and
  credentials. Legacy settings without consent fall back to local hints. Users
  can revoke consent and clear the key. Both native implementations restrict
  this consent to the official HTTPS DeepSeek endpoint.
- The owner will ask the friend to configure HTTPS on the existing Aliyun server.
  Deployment remains pending the resulting address and actual log/backup
  retention details. No live server changes or default-address switch occurred.
  The IP-certificate deployment plan and additive Nginx example are under
  `server/deploy/`; these files are preparation, not proof of deployment.
- Android Google Play debug and KMP Web compilation passed; Harmony direct
  debug packaging passed. AI privacy gates passed for all four Android
  distributions and Harmony. Policy copies and cross-platform tracking pass.
  Android emulator checks cover offline policy reading and consent controls.
  This does not validate production TLS or a newly uploaded Play release.
- Resolve the full-version privacy disclosure and safe transport using the same
  approved server before submitting policy-compliance declarations. The signing
  terms and export declaration also need the owner's review/confirmation at the
  relevant step. Do not silently select a different server or disable the full
  channel's room/AI functions.
- New personal-account production access requires the applicable closed test
  before public release. The published Google requirement is at least 12 testers
  opted in continuously for 14 days, followed by a production-access application;
  no testing completion or production eligibility has been claimed here.

References: [Google User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311),
[personal-account testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465).
