# Store publication status — 2026-09-24

F-Droid inclusion MR !49945 has been submitted and is open. No app store has
accepted or published this version yet. GitHub v1.6 is published. This record
supersedes the 2026-09-22 preparation snapshot.

## Public source and direct release

- Android release source: `e5129ab22ed6dc2c7baf7d5fd69225c3557ae4bd`, tag `v1.6`.
- [GitHub v1.6 release](https://github.com/WingSword/YiGameCopilotX/releases/tag/v1.6)
  is published with the signed direct APK. The v1.5 tag was not moved.
- [Upstream CI 35953950348](https://github.com/WingSword/YiGameCopilotX/actions/runs/35953950348)
  passed for all four signed Android APK distributions and the Play bundle.

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
  interaction works. Same-phone role reveal/hide and a virtual ledger transaction
  and undo were checked. No application crash was found in the crash buffer.
  These checks do not replace full game/server QA. The task's isolated emulator
  was stopped after testing; the user's existing emulator was left untouched.
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
- Uploaded the signed 1.6-domestic/code 10 APK, icon and four actual 1080x1920
  domestic screenshots. Description, release notes and reviewer instructions are
  filled. The completed questionnaire returned age 18+. Industry assessment is
  `其他分类 / 其他类`, with no additional special qualification field requested.
- [Domestic offline privacy policy](https://wingsword.github.io/YiGameCopilotX/store/vivo/privacy-domestic.html)
  is live over HTTPS and matches the reviewed generated HTML. GitHub Pages uses
  this publication branch's `/docs` directory. This URL is entered in vivo's
  custom-policy field; no generic vivo-hosted policy was published.
- Saved and reopened the draft: APK, images, text, age, offline filing choice,
  industry assessment and privacy URL were retained. Category `实用工具 / 常用工具`
  and `审核通过后立即发布` were not retained after reopening, including after a
  second selection/save. The edited page retains those selections; recheck them
  when the remaining required documents/contact fields allow final submission.
- The owner still needs to enter and verify contact email/phone in the official
  portal, as requested. No contact identity or verification code was fabricated.
- Offline undertaking requires the owner's signature and fingerprint, uploaded
  as JPG/PNG under 5 MB. A printable, unsigned one-page draft was generated and
  visually checked at `output/pdf/vivo-offline-undertaking-unsigned.pdf`. It is
  not a platform-issued template or a signed document, and was not uploaded.
- No official copyright certificate has been supplied. Do not substitute the
  project's Apache license for a copyright registration certificate.
- No review submission has been made.

## F-Droid

- GitLab account verification is complete. Personal public fork created:
  <https://gitlab.com/ZephyrSword/fdroiddata>.
- Candidate metadata uses current Game Helper / Party Game categories and
  upstream fastlane descriptions; optional DeepSeek is declared as NonFreeNet.
- `fdroid rewritemeta` and `fdroid lint` passed with fdroidserver 2.4.5 on Ubuntu
  WSL. Environment warnings concern absent Linux apksigner and mounted config
  permissions, not application metadata. This is not a build/scanner result.
- [Inclusion MR !49945](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945)
  is submitted and open, targeting `fdroid/fdroiddata:master`. It contains one
  metadata file and one commit, `f8c86651494083ec70f4a1f702071586476856ef`, from
  `ZephyrSword/fdroiddata:codex/org.walks.gamecopilot`.
- The recipe pins 1.6/code 10 to the full public source SHA above and enables
  version-tag updates. Only the latest version is included.
- Fork pipeline 2877427402 and MR pipeline
  [2877473132](https://gitlab.com/ZephyrSword/fdroiddata/-/pipelines/2877473132)
  both stopped with zero jobs and GitLab's `Verify your identity to run this
  pipeline` gate, explicitly separate from registration verification. The page
  also labels the pipeline invalid; no application build or scanner ran. The MR
  requests a maintainer-triggered FOSS pipeline as directed by the official
  template. Do not treat this as a successful build or repeat signup requests.
- Bit-for-bit reproducibility against an upstream fdroid-flavor APK is not
  established. The direct release is a different flavor, so no `Binaries` or
  `AllowedAPKSigningKeys` are set. Signing implications are disclosed in the MR.
- Attempted to attach the GitLab MR to the Codex task; the attachment tool rejected
  that URL as unsupported. The verified public MR link above remains the record.

## Google Play

- API 36 signed AAB is ready locally. Console still shows an expired login
  session; the owner has been asked to enter the developer console directly.
- No bundle has been uploaded. Developer-account eligibility, full-channel
  privacy/Data Safety, production HTTPS and release requirements remain open.
- Do not reuse the domestic offline privacy policy for Play or F-Droid, or claim
  the default HTTP room endpoint provides encrypted transport.
