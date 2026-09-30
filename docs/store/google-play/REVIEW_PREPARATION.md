# Google Play review preparation

Prepared September 30, 2026 for version 1.6.1/code 11. These are working notes,
not a submitted access declaration or an assigned age rating. Do not put any
API key, room session token, or personal account password in this repository.

## App access

The live Console says reviewers need all features and will not create an
account, use their own account, buy access, or start a trial. It explicitly
includes another-device operations and invitation codes as access restrictions.
The details dialog requires English instructions, a name of at most 60
characters, and at most 500 characters of additional information. Its checkbox
asserts full access to every feature, including paid content.

The app itself has no registration/login. Local play needs no account. Online
rooms need multiple participating clients and the room number/key. Optional
DeepSeek requests require a valid provider API key and explicit in-app consent;
an absent key falls back to local presets. That fallback does not establish
reviewer access to the real provider. Do not choose “no restrictions” or attest
full access until the reviewer setup is complete.

Proposed access entries, to finalize with verified infrastructure and a
dedicated review credential entered only in the official Console:

### Local tabletop tools

No app account is required. The interface is Chinese. Open the Games tab and
choose a game, then the one-phone mode. Follow the private-role prompts and pass
the device between players. Witch-hunt one-phone play needs a separate moderator.
Random tools, local virtual scorekeeping and preset hints work without a server
or a DeepSeek account. Virtual balances cannot be deposited, withdrawn or paid.

### Online rooms and LAN

Open the online lobby at the top right of Games, then choose online rooms.
Create a room with a nickname and room key. On other devices choose Join and
enter the same six-digit room number and key. All members must be ready; a shared
ledger needs at least two members and Spy needs at least four. LAN play requires
the same Wi-Fi network and an Android host kept in the foreground. Browser play
requires a separately deployed web client; do not promise one without checking.

### Optional DeepSeek hints

Open My, then Match hints. Enable hints, choose DeepSeek and confirm the data
disclosure. Enter the dedicated review API key supplied in this Console entry.
Keep the official https://api.deepseek.com endpoint, then request a hint in a
supported game. Local presets do not exercise the external AI feature. Revoke
consent and clear the key from the same settings page after testing.

No review API key is currently recorded or supplied. Do not submit the last
entry with a placeholder, instruct reviewers to create/pay for their own
account, or include a secret in the public store description. Revalidate the
production room endpoint before describing these steps as ready for review.

## Content-rating evidence to map to the actual questionnaire

- Avalon has an assassin action and text describing successful/failed
  assassination of Merlin: `awalong/SpecialAbilityDialog.kt` and
  `awalong/AwalongGamePageOptimized.kt` under the common Compose sources.
- Witch-hunt shows night death results in `ui/page/hunttown/HuntTownPage.kt`.
- One Night Werewolf includes the Drunk role in
  `shared/src/commonMain/kotlin/org/walks/gamecopilot/werewolf/data/WerewolfModels.kt`.
- The supplied Spy word list includes fruit wine/rice wine in
  `shared/src/commonMain/kotlin/org/walks/gamecopilot/data/LocalSpyWords.kt`.
- Room participants can exchange user-created drawings, guesses, player names,
  and ledger remarks. Optional AI produces text. Do not declare absence of
  user-generated or variable content without applying the form's definitions.
- The ledger is virtual scorekeeping, not a real-money financial service.
  This alone does not answer every rating question about simulated gambling;
  read the actual questionnaire and inspect the relevant preset interactions.

These source matches are preparation, not a complete visual/audio/content
audit. Inspect role artwork and any questionnaire-specific content before
claiming absence or intensity of violence, alcohol or other themes. Do not
invent an age rating or choose a target audience solely from the app category.

The live entry page states that completing the rating questionnaire means
agreeing to [IARC Terms of Use](https://web.iarcservices.com/terms). Owner consent
was requested during the afternoon follow-up and is pending. No questionnaire
was started and no rating was submitted. The agreement includes accurate and
updated answers, permitted use of ratings, and liability/dispute provisions.

Reference: [Google Play review access requirements](https://support.google.com/googleplay/android-developer/answer/10788890).
