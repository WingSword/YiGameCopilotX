# Publication follow-up — 2026-10-08

## Night check — 01:38–01:51 Shanghai

Read [October 7](PUBLISH_STATUS_20261007.md) before checking live sources.
Fresh public MR/pipeline APIs and the signed-in discussion confirm that
[F-Droid MR !49945](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945)
remains open and unmerged at recipe
`93f4d59eb808ec5a9d9207603eb8ec67b7cf4e8c`. Labels remain `New App` /
`review-requested`, with 18 user notes and last update
`2026-10-04T00:22:51.519Z`. The latest outside review remains
`note_3949994254`; our repair reply `note_3950227457` and description update
`note_3950239203` are still the latest discussion events. No new maintainer
question or manual-test result appeared.

No official pipeline for the submitted 1.6.2 revision is listed. Current
personal-fork pipeline 2909966368 remains failed with zero jobs, verified by
a fresh job-list response. Earlier official pipeline 2901381554 succeeded
for the previous 1.6.1 recipe; it does not validate the current head. The
[official package page](https://f-droid.org/en/packages/org.walks.gamecopilot/)
again returns HTTP 404, so no formal installable version is confirmed.
GitHub's latest public release remains v1.6.2/code 12, published October 4.
No new release, actionable build failure or metadata issue requires an
update. No duplicate application, repeated CI request or repeat build/test
run was made.

Google Play opened the known app dashboard but remained on the loading
shell, including a subsequent DOM snapshot. An AX observation timed out;
recovering the existing tab did not reveal app state. This run cannot
confirm current setup or tester counts. The last confirmed dashboard
snapshot is October 7: 7/11 setup tasks and zero opted-in testers; its
closed-testing and production restrictions remain last-known information,
not freshly verified facts. The vivo domestic draft `846668` again
redirects to official login, leaving its private review status unavailable.
No store submission, bundle upload or settings change occurred. Existing
owner dependencies have no new answers, and no repeated login, agreement,
materials, reviewer-access, tester or server-information reminder was sent.

Hosted HTTPS and server log/backup retention remain unverified. No transport
or retention declaration was invented. Existing 1.6.2 validation and its
device-testing limits remain as documented in the October 4 record.

Evidence is retained in `artifacts/store-publish-20261008/night/`: `mr.json`,
`pipelines.json`, `fork-jobs.json`, `package.html`, `upstream-release.json`
and `ui-observations.json`. The last file is a derived UI observation
summary, not a raw browser capture. Only this daily record is committed by
this follow-up; shared pending application, rename and Harmony work remains
intact. The six-hour heartbeat stays active. There is no important change or
new required owner action, so this check stays quiet.
