# Publication status — 2026-09-27

## F-Droid

[MR !49945](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945) is still
open and unmerged, pinned to recipe commit `f8d68d2075eac87ce50c5abc1a18a404b729a6c0`.
The official package page returns HTTP 404, so publication is not complete.

The maintainer's latest [reply](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3906114687)
requests that the rerun be checked. [Official pipeline 2883311450](https://gitlab.com/fdroid/fdroiddata/-/pipelines/2883311450)
finished successfully on September 26 at 01:07 Shanghai time. All nine jobs pass,
including [fdroid build](https://gitlab.com/fdroid/fdroiddata/-/jobs/16742363035)
and [check apk](https://gitlab.com/fdroid/fdroiddata/-/jobs/16742363043). This
supersedes the September 25 statement that an official build is still awaited.

The [quality report](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945/reports/code-quality)
contains 13 findings: one major cleartext-traffic warning, two minor notices
(INTERNET permission and no R8), and informational permission, size and Fastlane
metadata. These did not fail CI, but a passing pipeline is not maintainer
approval. The room endpoint's HTTP transport remains an actual unresolved issue.
The [follow-up reply](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/49945#note_3912147908)
acknowledges the nine passing jobs, discloses the remaining cleartext warning
and asks the maintainer to continue review.

Evidence from the public MR/jobs API is saved under
`artifacts/store-publish-20260927/`. No new source commit or recipe change is
needed merely to acknowledge a successful official build.

## Google Play

The developer account `5275652110070665181` is accessible and its identity
verification succeeded, as shown by its September 25 notification. The only
existing app shown is a different product; it was not modified.

The create form for 桌游助手 / `org.walks.gamecopilot` is filled with
Simplified Chinese, application type and free pricing. The package availability
check passes. The form is unsubmitted.
The policy-compliance and US export-compliance declarations are unchecked.
No AAB has been uploaded and no release has been submitted in this check.

Google Play listing preparation now includes matching Chinese/English feature
graphics (1024 × 500 RGB PNG), store icons converted to the required RGBA format
without altering their RGB pixels, existing real-device-format screenshots and
the existing bilingual text. See [prepared listing](google-play/README.md).
The full privacy policy is publicly hosted, but its production HTTPS and
retention details still need the server administrator's confirmed information.

Fresh read-only health requests returned:

| Address | Result |
| --- | --- |
| `http://8.133.216.39:8080/health` | HTTP 200, `status=ok`, `protocol=1` |
| `https://8.133.216.39:8080/health` | TLS handshake timed out |
| `https://8.133.216.39/health` | TLS ended without a valid connection |

These checks confirm that adding `s` alone does not produce a usable endpoint.
No certificate validation was disabled, and no default address was changed.
The owner previously chose to have the friend configure HTTPS. The working
endpoint and actual log/backup retention remain unavailable. After receiving
them, update/test both clients and policy and build the final signed Play AAB.
The old 1.6 AAB predates the new policy/AI-consent implementation.

Production release still depends on complete truthful declarations, the owner's
relevant legal confirmations, and any Console-required closed testing. No
production eligibility or public availability has been claimed.
