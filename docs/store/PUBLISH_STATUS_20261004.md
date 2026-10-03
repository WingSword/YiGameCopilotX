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
