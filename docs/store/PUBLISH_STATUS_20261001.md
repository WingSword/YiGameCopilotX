# Publication follow-up — 2026-10-01

## Night check — 01:25–01:31 Shanghai

The original [F-Droid MR !49945](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945)
remains `opened`, with recipe head
`737a28b9ab8a45dd5417b07716de3398ff51fcea`, 13 user notes and last update
`2026-09-29T18:24:24.779Z`. The unchanged update time and note count provide no
indication of new maintainer feedback since the last checked discussion. No
duplicate reply or rerun request was posted.

The MR pipeline list still has no official run for the current revision. Fork
run `2894481258` remains failed; official run `2889952044` remains successful
for the older `fa3e3db2` recipe. Its success must not be attributed to the
current revision. The official package page returned HTTP 404 again, so no
official installable F-Droid version is confirmed.

The live Google Play dashboard for app `4975340793676853600` still shows
**7/11** setup tasks complete and **0 opted-in testers**. App access, content
rating, target audience and data safety remain unfinished. The closed test is
still locked pending application setup. No answer to the previously requested
IARC consent or dedicated DeepSeek review credential is present in this task;
the tester and server-fact dependencies also remain. No agreement, declaration,
bundle upload, closed test or release was submitted in this check.

The vivo domestic draft `846668` again redirects to the official login page.
Its private review status cannot be verified. No repeated login or material
request was sent.

There are new, pre-existing uncommitted workspace changes preparing the Chinese
name **易玩桌游**, including app labels, privacy documents, Fastlane text and
the local F-Droid `AutoName`. These changes were preserved and were not staged,
published or added to the existing F-Droid application by this heartbeat.
The local Google Play worksheet explicitly requires a rebuilt package and
refreshed captures/graphic for the rename. The live Play app still displays
**桌游助手**. Future publication work must reconcile the final validated rename
with the package and store assets rather than claiming it already shipped.

The LAN startup/broadcast defects were already fixed and tested in v1.6.1;
see [the September 30 record](PUBLISH_STATUS_20260930.md) for the exact coverage
and limits. No new runtime defect or source change from this heartbeat required
repeating those builds or tests.

Evidence: `mr-night.json`, `pipelines-night.json`,
`fdroid-package-night.html` and `other-stores-night.json` under
`artifacts/store-publish-20261001/`. Existing requests remain pending without
duplicate reminders. The six-hour follow-up continues quietly because no
material store status change or new owner action was found.

## Morning check — 07:26 Shanghai

Fresh MR and pipeline API reads are unchanged: `opened`, head `737a28b9`,
13 user notes, last update `2026-09-29T18:24:24.779Z`; no new official run for
the current head. The old official `2889952044` remains successful and the
current fork `2894481258` remains failed. The official package page again
returns HTTP 404. No new review feedback or confirmed installable release was
found, and no duplicate comment was sent.

After refreshing the live Play dashboard, setup remains **7/11**, with the same
four unfinished tasks, **0 opted-in testers**, closed testing locked and the
production application button disabled. The saved app name is still
**桌游助手**. The vivo domestic draft again redirects to login. Existing
consent, reviewer-access, tester, server and vivo dependencies remain pending;
no duplicate owner reminder, agreement acceptance or submission was made.
The pre-existing uncommitted rename work was left untouched.

Evidence in `artifacts/store-publish-20261001/`: `mr-morning.json`,
`pipelines-morning.json`, `fdroid-package-morning.html`,
`play-dashboard-morning.txt` and `vivo-morning.json`. The follow-up remains
active, with no new actionable change requiring notification.
