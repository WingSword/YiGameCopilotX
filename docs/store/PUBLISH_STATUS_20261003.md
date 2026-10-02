# Publication follow-up — 2026-10-03

## Night check — 01:32–01:38 Shanghai

Fresh public APIs and the refreshed signed-in discussion confirm that the
original [F-Droid MR !49945](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945)
remains open and unmerged, with recipe head
`737a28b9ab8a45dd5417b07716de3398ff51fcea`, labels `New App` /
`review-requested`, 16 user notes, and last update
`2026-10-02T05:42:20.689Z` (yesterday's description update).

The latest maintainer feedback is still the October 2 10:43
[manual-testing queue note](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3942483660).
There is no new review question to answer. No duplicate comment, application or
pipeline rerun request was posted.

The latest official pipeline remains
[2901381554](https://gitlab.com/fdroid/fdroiddata/-/pipelines/2901381554), successful
for that exact recipe. A fresh jobs read confirms **all nine jobs successful**.
The personal-fork failure is not a new application-build failure. The
[official package page](https://f-droid.org/en/packages/org.walks.gamecopilot/)
again returns **HTTP 404**, so no official installable version or publication
is confirmed. Upstream's latest published release remains **v1.6.1**, pinned by
the current recipe; no new release requires updating this MR.

The refreshed Google Play dashboard for app `4975340793676853600` still displays
**桌游助手**, **7/11** setup tasks complete and **0 opted-in testers**. App
access, content rating, target audience and data safety remain unfinished.
Closed testing is locked and the production application button is disabled.
An unrelated optional Console feedback survey is visible; it was not submitted
or accepted. No bundle upload, agreement acceptance or store submission was
performed.

The vivo domestic draft `846668` again shows its official login page. Private
review status remains unavailable. The existing owner dependencies for both
stores have no new answers in this task; no repeated login, consent, credential,
tester or material request was sent. Unknown server HTTPS and retention facts
remain unknown.

Shared application, rename and Harmony preparation changes were preserved.
This follow-up creates only this daily record. No new source/metadata problem
requires further code changes or repeating the validated v1.6.1 builds/tests.
See [the October 2 record](PUBLISH_STATUS_20261002.md) for the current official
CI evidence and [September 30](PUBLISH_STATUS_20260930.md) for LAN test limits.

Evidence is retained under `artifacts/store-publish-20261003/night/`:
`mr.json`, `pipelines.json`, `official-jobs.json`, `mr-comments.txt`,
`package.html`, `upstream-release.json`, `play-dashboard.txt` and `vivo.txt`.
The six-hour follow-up remains active. With no material change or new required
owner action, this check stays quiet.
