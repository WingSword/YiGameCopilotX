# Publication follow-up — 2026-10-09

## Night check — 01:45–01:56 Shanghai

Read [October 8](PUBLISH_STATUS_20261008.md) before checking live sources.
Fresh public APIs show [F-Droid MR !49945](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945)
still open and unmerged at recipe
`93f4d59eb808ec5a9d9207603eb8ec67b7cf4e8c`, with `New App` /
`review-requested`. User notes increased from 18 to 19; the last update is
`2026-10-08T16:11:45.638Z`.

The signed-in discussion contains a new [static follow-up by mezinster](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3979101136)
at 00:11 Shanghai. It confirms all three upstream fixes: release Compose
preview tooling, shared-preference backup exclusions covering the AI key,
and removal of the retired room-client chain. The reviewer considers the
author-side work complete and identifies a maintainer-triggered official
pipeline for the current recipe as the next step before on-device testing.
There is no new author-side defect or question to answer. No redundant
acknowledgment or repeat CI request was posted.

This is static contributor feedback, not completed runtime testing, a merge,
or publication. The comment's VirusTotal link names a code-11 APK and uses
the earlier `0b1aeef4...` hash despite referring to a 1.6.2 release asset;
it is not used as new 1.6.2 artifact-validation evidence here. Existing
validation limits remain as recorded on October 4.

The fresh pipeline list still has no official run for the submitted 1.6.2
recipe. Personal-fork pipeline 2909966368 remains failed with zero jobs,
confirmed by its current job-list response. The earlier successful official
pipeline 2901381554 covers 1.6.1 only. The
[official package page](https://f-droid.org/en/packages/org.walks.gamecopilot/)
still returns HTTP 404, so no formal installable version is confirmed.
GitHub's latest public release remains v1.6.2/code 12. No metadata update,
new source release, duplicate application or repeat build was needed.

The live Google Play dashboard loaded successfully this time and confirms
7/11 completed setup tasks and zero opted-in testers. App access, content
rating, target audience and data safety remain unfinished; closed testing
requires the remaining setup and the production application button is
disabled. The vivo domestic draft `846668` again redirects to official
login, leaving its private review state unavailable. No store submission,
bundle upload or settings change occurred. Existing unanswered owner
dependencies were not asked again. Hosted HTTPS and server log/backup
retention remain unverified; no declaration was invented.

Updated the existing `automation` heartbeat with the new review note and
fresh Google Play state. Its ACTIVE status, target chat and configured
24-hour interval are preserved. Earlier records described a six-hour
cadence; that does not match the currently saved schedule. No new automation
was created. This meaningful review progress warrants a concise notification;
unchanged future checks should remain quiet.

Evidence is retained in `artifacts/store-publish-20261009/night/`: `mr.json`,
`pipelines.json`, `fork-jobs.json`, `package.html`, `upstream-release.json`
and `ui-observations.json`. The last file summarizes actual UI observations
and is not a raw browser capture. MR and vivo observations were recovered
after page-observation/navigation timeouts. Only this daily record is
committed by this follow-up; shared pending application, rename and Harmony
work remains intact.
