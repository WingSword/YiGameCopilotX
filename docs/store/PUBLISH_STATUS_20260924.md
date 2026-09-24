# Store publication status — 2026-09-24

No store has accepted or published this version. This record supersedes the
2026-09-22 preparation snapshot; store-side tasks are still in progress.

## Verified release candidates

- Android 1.6 / code 10: signed domestic and F-Droid APKs and Google Play AAB.
  The APK certificates and the AAB certificate match the configured release key.
  Both 64-bit APK native libraries and APK ZIP entries pass 16 KB alignment checks.
- Domestic Android removes INTERNET and ACCESS_NETWORK_STATE. Its UI and AI
  factory use local hints only. Full distribution channels retain online AI.
- Domestic lint: 0 errors, 34 warnings, 2 hints. Release-verifier tests: 12 pass,
  1 optional fixture skip; the actual APKs were separately verified with SDK tools.
- API 36 isolated emulator: domestic home has no lobby; local settings have no
  DeepSeek/key selector; the bundled privacy policy opens and closes; answer-book
  interaction works. These smoke checks do not replace full game/server QA.
- Wasm compilation passed. Harmony 1.6 / code 11 domestic release built and its
  signature/profile were checked; INTERNET and GET_NETWORK_INFO are absent from
  the packaged manifest. No new Harmony device test was performed.
- Cross-platform review passed for 13 groups, including generated policy text.
- Local candidates, SHA256SUMS and signature evidence are under
  `artifacts/store-publish-20260924/`; these files are not committed to Git.

## vivo

- Domestic draft: <https://dev.vivo.com.cn/appPerfect/846668>, App-ID 106154806,
  package `org.walks.gamecopilot.domestic`. The prior 844098 full draft remains.
- The owner authorized removing optional online AI. The verified 1.6 domestic
  package now qualifies for evaluation as genuinely offline functionality.
- Final offline policy is `vivo/privacy-domestic.txt`, generated into Android,
  Harmony and `vivo/privacy-domestic.html`. It uses the approved public operator
  and support email. It does not cover full distribution channels.
- Age questionnaire, final package/assets, hosted policy, qualification form,
  offline undertaking and contact verification remain in progress. A template or
  unsigned undertaking must not be represented as a signed document.
- No review submission has been made.

## F-Droid

- GitLab account verification is complete. Personal public fork created:
  <https://gitlab.com/ZephyrSword/fdroiddata>.
- Candidate metadata uses current Game Helper / Party Game categories and
  upstream fastlane descriptions; optional DeepSeek is declared as NonFreeNet.
- `fdroid rewritemeta` and `fdroid lint` passed with fdroidserver 2.4.5 on Ubuntu
  WSL. Environment warnings concern absent Linux apksigner and mounted config
  permissions, not application metadata. This is not a build/scanner result.
- Update the recipe to the new public 1.6 source commit, validate in the fork
  pipeline, and submit the inclusion merge request. No merge request exists yet.
- Bit-for-bit reproducibility against the upstream APK is not yet established.

## Google Play

- API 36 signed AAB is ready locally. Console still shows an expired login
  session; the owner has been asked to enter the developer console directly.
- No bundle has been uploaded. Developer-account eligibility, full-channel
  privacy/Data Safety, production HTTPS and release requirements remain open.
- Do not reuse the domestic offline privacy policy for Play or F-Droid, or claim
  the default HTTP room endpoint provides encrypted transport.
