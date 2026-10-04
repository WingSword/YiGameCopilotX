# Publication follow-up — 2026-10-04

## Night check — 01:34–01:40 Shanghai

Fresh public APIs and the live signed-in discussion confirm that the original
[F-Droid MR !49945](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945)
remains open and unmerged, with recipe
`737a28b9ab8a45dd5417b07716de3398ff51fcea`, labels `New App` /
`review-requested`, 16 user notes and last update
`2026-10-02T05:42:20.689Z`. The latest maintainer feedback remains the
[October 2 manual-testing queue note](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3942483660).
No new review question, comment or merge has appeared.

Current official pipeline
[2901381554](https://gitlab.com/fdroid/fdroiddata/-/pipelines/2901381554) remains
successful for that exact recipe. A fresh job-detail response confirms all nine
jobs successful. The [official package page](https://f-droid.org/en/packages/org.walks.gamecopilot/)
again returns HTTP 404. No official installable version or store publication
is confirmed. Upstream's latest published release remains v1.6.1, so no new
release requires changing the submitted recipe. No repeated comment, rerun
request or additional application was submitted.

The live Google Play dashboard for app `4975340793676853600` still displays
桌游助手, 7/11 setup tasks complete and 0 opted-in testers. App access,
content rating, target audience and data safety remain unfinished. Closed
testing is locked and production access is disabled. The vivo domestic draft
`846668` still redirects to the official account login page; private review
status remains unavailable.

Existing owner dependencies have no new answers in this task. No repeated
login, terms-consent, reviewer-access, tester or material request was sent.
No store agreement, bundle upload or release was submitted. Unknown HTTPS,
server logs and backup-retention facts were not filled as known facts.

A stale MR tab failed to attach during refresh and disappeared from the tab
list. The browser connection remained valid; creating a fresh tab in the same
browser recovered the original discussion. The other two store pages loaded
normally. The transient tab error did not block this check.

Shared uncommitted application, rename and Harmony work was preserved. Only
this daily record is included in this follow-up's repository commit. There is
no new code/metadata failure requiring source changes or repeat build/test
runs. See [October 2](PUBLISH_STATUS_20261002.md) for official CI evidence and
[September 30](PUBLISH_STATUS_20260930.md) for the v1.6.1 LAN test coverage.

Evidence is retained in `artifacts/store-publish-20261004/night/`: `mr.json`,
`pipelines.json`, `official-jobs.json`, `mr-comments.txt`, `package.html`,
`upstream-release.json`, `play-dashboard.txt` and `vivo.txt`. The six-hour
follow-up remains active. There is no meaningful publication change or new
required owner action, so this check stays quiet.

## Morning follow-up — new static review and v1.6.2

The 07:36 Shanghai heartbeat found a new
[contributor review by Evgeny Mezin](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3949994254),
posted at 05:04 Shanghai. The review reports three non-blocking upstream issues:
release Compose tooling exposes PreviewActivity; optional AI credentials can
enter Android preference backups; obsolete HTTP/WebSocket room-client code
remains compiled. The contributor's static review is favorable overall, but
on-device testing and startup traffic capture remain pending. Its reported
third-party malware scan is not a scan performed by this task.

The three findings are fixed in source commit
[`871edeab020ca1dd5399f92af3a42621db62f0a9`](https://github.com/WingSword/YiGameCopilotX/commit/871edeab020ca1dd5399f92af3a42621db62f0a9),
published as [v1.6.2 / code 12](https://github.com/WingSword/YiGameCopilotX/releases/tag/v1.6.2)
at 08:02:31 Shanghai:

- Compose ui-tooling is now a debug dependency. Release manifests and DEX files
  no longer contain PreviewActivity.
- Android legacy full-backup rules and Android 12+ cloud/device-transfer rules
  exclude shared preferences, including optional AI credentials and room
  sessions. File-level exclusion also omits other preferences stored alongside
  them. Existing backups are not erased; other files remain eligible for backup.
- The unused legacy room client, models, UI and startup subscriptions were
  removed. The old `116.198.196.244` endpoint is absent from tracked sources and
  all four release DEX files. Current cloud rooms and LAN flows are retained.
- A release-artifact checker is included in signed-build CI to verify the
  absent tooling/legacy endpoint and the manifest's references to backup rules
  with all required exclusions. It resolves renamed resources after shrinking.

Validation used an isolated clean checkout, not the shared workspace's pending
rename/Harmony changes. All four signed, minified APK builds and the Google
Play AAB build pass. Local channel/package/version/certificate checks pass,
as do the new artifact checks and optional-AI privacy-gate checks for all four
channels. The prior APK fails the new PreviewActivity check, providing a
negative check for that finding. Existing release-verification unit tests pass
(13 tests, one optional SDK test skipped). Local fdroidserver 2.4.5 metadata
lint/format and exact-source scanning after the recipe's prebuild report zero
errors; the existing font warning remains. This is local build/scanner evidence,
not an official Linux build or a reproducibility claim. No new device test,
startup traffic capture or physical backup-transfer test was performed.

The local fdroid APK is 12,687,276 bytes with SHA-256
`eb7840a46b5aae7a7b6a96ce44b7ff82e3fb79a5a9e1ff860c6c691cdc6da713`.
The [GitHub signed-build workflow 37163371066](https://github.com/WingSword/YiGameCopilotX/actions/runs/37163371066)
also completed successfully for all four channels at 08:03:25 Shanghai.
The public GitHub release contains the direct APK; this does not establish
publication on any target app store.

The existing F-Droid branch was updated, without creating another application,
to recipe `93f4d59eb808ec5a9d9207603eb8ec67b7cf4e8c`, pinning the published
v1.6.2 source. Only its version/code/source fields changed. Submitted AutoName
remains 桌游助手; the uncommitted local 易玩桌游 rename is preserved separately.
The maintainer-facing
[reply](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3950227457)
was posted at 08:15:14 Shanghai, describing fixes, validation and limits and
requesting a new official pipeline. The original MR description was updated
and its complete text, head SHA, open state, title and labels verified through
the public API at 08:22:51 Shanghai.

The current personal-fork
[pipeline 2909966368](https://gitlab.com/ZephyrSword/fdroiddata/-/pipelines/2909966368)
failed with zero jobs; this is not an application-build failure. The prior
official nine-job success, pipeline 2901381554, covers 1.6.1 and is explicitly
not claimed for the new recipe. A final pipeline-list refresh shows no official
run for the new head yet. MR !49945 remains open with `New App` and
`review-requested`; the formal package page still returns HTTP 404. Official
CI for 1.6.2 and manual testing are pending. No acceptance or installable
F-Droid version is confirmed.

Fresh Google Play evidence still shows 7/11 setup tasks and zero opted-in
testers, with closed testing locked and production unavailable. The vivo
domestic draft still redirects to login. No store bundle upload, agreement or
review submission was made. Previously recorded unanswered owner dependencies
were not asked again. The optional hosted room endpoint remains the friend's
Aliyun `http://8.133.216.39:8080`; HTTPS and server log/backup retention remain
unverified. The current LAN and hosted transport limitations remain disclosed.
Domestic remains offline, while the international/direct/F-Droid channels
retain their full feature configuration.

Detailed evidence and all four local APKs plus the AAB are retained under
`artifacts/store-publish-20261004/morning/`. Key files include `review-build.log`,
`ai-privacy-checks.log`, channel review/signature JSON, `source-scan.json`,
`github-new-run.json`, `github-new-jobs.json`, `upstream-1.6.2-release.json`,
`mr-after-recipe.json`, `mr-after-description.json`, `pipelines-final.json`,
`package-final.html`, `review-reply.md`, `mr-description-1.6.2.md` and
`fdroid-1.6.2-review-reply.png`. Shared pending changes remain intact. The
temporary checkout's needed artifacts were preserved before archiving it.
The existing six-hour heartbeat is updated to follow the new recipe and reply,
without repeatedly requesting CI or resubmitting the application.
