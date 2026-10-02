# Publication follow-up — 2026-10-02

## F-Droid: official checks passed, awaiting manual testing

The original [MR !49945](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945)
remains open and unmerged. The current recipe is
`737a28b9ab8a45dd5417b07716de3398ff51fcea`, pinning upstream
`1ed22875bfeb8b603ff1fcbfe545239b6f762138` / **v1.6.1, version code 11**.

[Official pipeline 2901381554](https://gitlab.com/fdroid/fdroiddata/-/pipelines/2901381554)
ran in F-Droid project `36528` for that exact recipe and completed successfully
on **October 1 at 21:06:58 Shanghai**. All nine jobs passed:

| Job | ID | Result |
| --- | --- | --- |
| fdroid build | 16867013249 | success |
| checkupdates | 16867013250 | success |
| git redirect | 16867013251 | success |
| fdroid lint | 16867013252 | success |
| fdroid rewritemeta | 16867013253 | success |
| tools check scripts | 16867013254 | success |
| schema validation | 16867013255 | success |
| check source code | 16867013256 | success |
| check apk | 16867013257 | success |

This is the requested official rerun for the current revision. The earlier
personal-fork pipeline `2894481258` failed before the application build because
of the fork's CI identity gate; it does not describe this successful official
run. The older official pipeline `2889952044` covered a different recipe.
No personal GitLab phone/card verification or repeated rerun request is needed
to continue this inclusion route.

The overnight check answered the maintainer's request to inspect the result in
[note 3939865720](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3939865720)
at **01:38 Shanghai**, confirming all nine jobs and retaining the disclosed
cleartext-room limitation. The readback confirmed the reply; it was not reposted.

At **10:43 Shanghai**, maintainer linsui posted
[note 3942483660](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3942483660):
the application is mostly ready, will undergo manual testing, and may be merged
if testing succeeds. The review queue is long; no publication date was given.
The label changed from `waiting-on-response` to `review-requested`. The
maintainer also requested updating this MR if a new upstream version is released.
The public upstream latest-release API still reports **v1.6.1** this afternoon;
uncommitted rename preparation is not a new release.

During the **13:30–13:49 Shanghai** follow-up, the MR description and validation
checklist were updated to reflect current official CI success and pending manual
review. The save is recorded at **13:42:20 Shanghai**, and public API readback
matches the intended description exactly. The title, recipe head and maintainer
labels remain unchanged. No duplicate comment, new application, recipe change
or unrelated review work was performed.

The [official package page](https://f-droid.org/en/packages/org.walks.gamecopilot/)
returned **HTTP 404** during both today's overnight and afternoon checks.
There is still no confirmed official installable F-Droid version. Successful CI
and a positive preliminary review are not a merge or publication confirmation.

## Google Play and vivo

The live Google Play dashboard for app `4975340793676853600` still shows
**7/11** setup tasks complete and **0 opted-in testers**. App access, content
rating, target audience and data safety remain incomplete. Closed testing is
locked pending application setup; production access is disabled. The displayed
name remains **桌游助手**. No bundle, review or release was submitted in this
check.

Previously requested IARC terms consent, dedicated AI reviewer access, tester
details and verified server HTTPS/log/backup facts have no new answers in this
task. These facts were not invented and the same questions were not sent again.

The vivo domestic draft `846668` still displays the official login page. Private
review status remains unavailable. Existing contact verification, signed offline
undertaking and qualification-material dependencies remain recorded; no repeated
login/material reminder or store submission was made.

## Continuity and verification

The domestic offline / international full-feature channel plan is unchanged.
The friend-hosted room endpoint remains the previously recorded HTTP endpoint;
HTTPS and server retention facts are not verified. The LAN creation/broadcast
fixes are already included in v1.6.1; their targeted test coverage and limits
are in [the September 30 record](PUBLISH_STATUS_20260930.md). No new code defect
or source change in this follow-up justified repeating those tests/builds.

Pre-existing workspace edits, including the pending **易玩桌游** rename and
other publication documents, were preserved. Only this new daily record is
included in this heartbeat's repository commit.

The existing six-hour heartbeat `automation` remains active in the original
chat. Its baseline was updated to the successful current official pipeline and
manual-test queue, replacing the stale rerun/LAN-defect baseline. It retains
the same schedule and quiet-on-no-change behavior; no duplicate automation was
created. Next checks should inspect new feedback, merge status and the official
package/version, and update the existing MR only for an actual new release or
actionable review issue.

Evidence is retained under `artifacts/store-publish-20261002/`:

- `mr-night.json`, `pipelines-night.json`, `official-pipeline.json`,
  `official-jobs.json`, `mr-comments-before.txt`, `mr-comments-after.txt`,
  `maintainer-reply.md` and `fdroid-reply-posted.png` capture the initial result
  and posted acknowledgment.
- `mr-afternoon.json`, `pipelines-afternoon.json`, `mr-comments-afternoon.txt`
  and `upstream-release.json` capture the new manual-review state.
- `mr-description-updated.md`, `mr-after-update.json`, `mr-after-update.txt`
  and the visually inspected `fdroid-current-review.png` confirm the description
  update without changing the source revision.
- `fdroid-package-night.html`, `fdroid-package-afternoon.html`,
  `play-dashboard-afternoon.txt` and `vivo-afternoon.txt` retain publication and
  other-store observations.

The description was validated by exact API readback, all nine official job
statuses were checked, and the new record was checked for whitespace errors.
