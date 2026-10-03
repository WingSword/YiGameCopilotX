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

## Morning check — 07:33–07:37 Shanghai

Fresh MR/pipeline APIs and the refreshed signed-in discussion show no new
maintainer comment, merge or review change: `opened`, head `737a28b9`,
`review-requested`, 16 user notes and last update `2026-10-02T05:42:20.689Z`.
Official pipeline `2901381554` remains successful; a fresh jobs read confirms
all nine successful jobs. The official package page again returns HTTP 404,
and upstream's latest published release remains v1.6.1. No repeated comment,
rerun request or recipe update is needed.

The refreshed Play dashboard remains at 7/11 setup tasks, with the same four
unfinished items, 0 opted-in testers, closed testing locked and production
access disabled. The vivo domestic draft again redirects to login. Previously
requested owner information and confirmations have no new answers in this
task; no repeated request or store submission was made. Shared uncommitted
application/Harmony/rename work was preserved.

Evidence is retained in `artifacts/store-publish-20261003/morning/`: `mr.json`,
`pipelines.json`, `official-jobs.json`, `mr-comments.txt`, `package.html`,
`upstream-release.json`, `play-dashboard.txt` and `vivo.txt`. Only this record
is changed by the check. The six-hour follow-up remains active and quiet while
the review state and existing owner dependencies remain unchanged.

## Afternoon check — 13:34–13:41 Shanghai

Fresh MR and pipeline-list APIs plus the live discussion confirm no new review
change: `opened`, unmerged, head `737a28b9`, `review-requested`, 16 user notes
and last update `2026-10-02T05:42:20.689Z`. The latest maintainer response is
still the manual-testing queue note. Current official pipeline `2901381554`
remains `success` for the same head, with no newer run. The package page again
returns HTTP 404 and upstream's latest published release remains v1.6.1.
No comment, rerun request or recipe update was needed.

The pipeline-list request had a connection timeout, then succeeded on one
retry. The separate job-detail request timed out on both attempts; its most
recent complete nine-job confirmation is this morning's saved response.
This check establishes current pipeline success from fresh MR and pipeline-list
responses, and does not claim a new successful job-detail fetch. An empty or
incomplete `official-jobs.json` from those failed requests is not evidence.

The live Play dashboard remains at 7/11 setup tasks, with 0 opted-in testers,
closed testing locked and production access disabled. The optional Console
survey was not accepted or submitted. The vivo domestic draft still shows
the official login page. Existing owner dependencies remain unanswered; no
duplicate reminder or store submission was made. Shared uncommitted code,
rename and Harmony work was preserved.

Evidence is in `artifacts/store-publish-20261003/afternoon/`: `mr.json`,
`pipelines.json`, `mr-comments.txt`, `package.html`, `upstream-release.json`,
`play-dashboard.txt` and `vivo.txt`. Only this record is committed. There is
no material publication change or new required owner action; follow-up stays
active and quiet.
