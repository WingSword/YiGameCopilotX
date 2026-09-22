# Google Play declarations worksheet

Preparation only. Do not submit these notes as final declarations or claim that
the application collects no data. The operator approved public use of the vivo
contact name and `YvesSword@outlook.com` on 2026-09-22.

| Feature | Source-backed handling | Console review |
| --- | --- | --- |
| Local games/tools/ledger | Local names, settings, words, results and virtual transactions; Android backup is enabled | On-device processing and OS backup must be distinguished from app-server collection |
| Cloud rooms | Nickname, random member ID, session token, room actions, votes, drawings, guesses, virtual ledger entries | Review name/user ID, app activity and user-generated-content categories; collection is conditional on using rooms |
| Room sharing | Public room state is visible to participants; private roles are sent according to game phase | Apply the form's user-initiated-sharing definitions; do not mark every flow as unshared without reading the guidance |
| Network/abuse controls | Connection IP is available to the server; code uses it for rate limiting | Confirm production logs, retention, access controls and any IP-derived processing |
| Optional DeepSeek | A user-supplied key and requested game context go to the configured provider when enabled | Review third-party handling and provide an accurate in-app disclosure before transmission |

There is no application account-registration flow, no real-money deposit or
withdrawal, and no advertising/analytics SDK found in the reviewed dependency
declarations. Virtual game balances are not actual financial accounts.

Required remaining facts and product work:

- Confirm the production HTTPS endpoint; the current default is HTTP, so the
  Data Safety “encrypted in transit” answer cannot truthfully be “yes”.
- Confirm production logs/backups and deletion periods. Source code expires
  inactive rooms after 12 hours and retains up to 1,000 match summaries; neither
  statement proves the deployed server's logging/backup behavior.
- Publish a complete, public HTML privacy policy and expose the same policy
  inside the app. The existing vivo policy is a draft with unresolved items.
- Complete content rating and target-audience questionnaires from the actual
  features; do not invent an age rating or claim the app is designed for children.
- Inspect the authenticated Play Console for developer verification, package
  name/version availability, signing setup and any testing prerequisites.
  New personal accounts may require 12 continuously opted-in testers for 14
  days before applying for production access; no account-specific conclusion
  is possible until the console is accessible.

References: [User Data](https://support.google.com/googleplay/android-developer/answer/10144311),
[Data Safety](https://support.google.com/googleplay/android-developer/answer/10787469),
[personal-account testing](https://support.google.com/googleplay/android-developer/answer/14151465).
