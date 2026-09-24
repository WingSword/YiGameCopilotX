# Store publication status — 2026-09-22

Historical snapshot. See [2026-09-24 status](PUBLISH_STATUS_20260924.md) for the
offline 1.6 packages, completed GitLab verification and current publication work.

**No store has accepted or published this version yet.** This document records
completed preparation, not an approval or a claim of production readiness.

## vivo

- Created a separate domestic application record at
  <https://dev.vivo.com.cn/appPerfect/846668> (App-ID `106154806`). Package:
  `org.walks.gamecopilot.domestic`. The old full-package draft `844098` remains.
- Uploaded `YiGameCopilotX-1.5-domestic.apk` from the earlier channel build.
  The portal recognized a 32/64-bit package, version `1.5-domestic`, code `9`.
  This was a package preflight; no review submission was made.
- The uploaded APK predates the subsequent system-bar theme fix and still
  permits optional DeepSeek. Replace it with the final checked domestic build.
- [vivo FAQ, question 4](https://dev.vivo.com.cn/documentCenter/doc/722)
  permits genuinely offline apps to select “否 → 单机” with a signed offline
  undertaking. The portal then performs an offline test. Hiding rooms alone
  does not justify making that declaration while optional AI still connects.
- Owner decision to disable domestic online AI is pending. Do not select the
  offline declaration or sign an undertaking before that decision and verification.
- Complete the privacy policy/in-app entry, final screenshots, categorization,
  age rating and qualification assessment. Contact verification is user-operated.
- New draft description editing encountered browser focus/clipboard failures;
  the replacement copy exists in `vivo/STORE_COPY_DOMESTIC.md`, but its successful
  save in the new portal record has not been established.

## Google Play

- compileSdk is now 36; only Google Play targets API 36. Other channels remain
  target 35. The release verifier rejects old/missing Play target levels.
- Rebuilt signed APK/AAB. APK signature and bundle certificate match the
  configured signer. Both 64-bit native libraries use 16 KB load alignment;
  APK ZIP page alignment also passes.
- Google Play lint: 0 errors, 38 warnings, 2 hints. Release verifier tests:
  12 passed and 1 optional real-APK fixture test skipped; the actual signed
  candidates were separately verified using the Android SDK tools.
- API 36 emulator (isolated `YiGameStoreAPI36`, serial `emulator-5566`) checked
  startup, lobby entry/back, tools and light/dark theme switching. Fixed light
  status-bar contrast and the dark navigation-area background. This is not
  a new end-to-end validation of the production room server.
- Console access was blocked by Google's identity verification; the login tab
  now reports an expired session. No AAB has been uploaded and developer-account
  eligibility is not yet known.
- Privacy, production HTTPS/data retention and account-specific release steps
  remain open. See `GOOGLE_PLAY_DATA_SAFETY_DRAFT.md`.

## F-Droid

- Owner selected Apache-2.0; root LICENSE and README updated. Existing third-party
  notices retained. Source and license are public at commit
  `43147662bd8a320fea0df7c7fb12f5b33d9436ee` on
  [`codex/store-publication-20260922`](https://github.com/WingSword/YiGameCopilotX/tree/codex/store-publication-20260922).
- Signed F-Droid candidate built and checked; app retains full room features.
  API 36 emulator screenshots were captured from the actual F-Droid artifact.
- Added English/Chinese fastlane metadata, icon, screenshots and candidate build
  metadata pinned to that exact public commit.
- This is not an official F-Droid build or an inclusion submission. Clean Linux
  upstream builds with official repositories passed in GitHub Actions; an unsigned
  F-Droid build and official scanner/lint checks are still needed. Bit-for-bit
  reproducibility has not been established.
- GitLab login is confirmed. Mandatory account initialization requires company,
  group, project and country/region; the observed country list does not include
  China. The user was asked to finish setup using truthful information. No fork
  or merge request has been created. Do not repeat the login request.

## Shared evidence and remaining release work

- [GitHub Actions run 35742097588](https://github.com/WingSword/YiGameCopilotX/actions/runs/35742097588)
  completed successfully for source `43147662bd8a320fea0df7c7fb12f5b33d9436ee`:
  `direct`, `domestic`, `googlePlay`, and `fdroid` APKs plus the Play AAB.
  All APK signature/manifest verification steps passed. This dispatch created
  CI artifacts, not a GitHub Release or store submission.
- Wasm compilation passed after adding the native-system-bar hook; Wasm/iOS
  implementations are no-ops. Harmony already updates its system-bar colors in
  `Index.ets.onThemeChange`; no new Harmony or iOS device test was performed.
- Cross-platform review passed for all 13 feature groups, including 6,010
  random-tool and 10,206 layout parity checks. This does not certify pixel parity.
- Public privacy operator/contact confirmed; deployment and policy text still
  contain unresolved facts and must not be published as a final privacy policy.
- New candidates and hashes: `artifacts/store-publish-20260922/release/`.
  They are separate from the earlier `artifacts/distribution-20260922/release/`.
- Current Android candidates are still version 1.5/code 9. Existing `v1.5` tag
  points at older code. Assign a new release version/tag before final publication;
  never move the old tag or claim it contains the new source.
