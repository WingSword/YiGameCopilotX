# Google Play submission preparation

Checked on 2026-09-30. The live console now contains the separate 桌游助手 app
`4975340793676853600` under developer `5275652110070665181`. Its privacy policy
URL has been saved; no release or complete review submission is confirmed.
The dashboard requires the remaining application setup and a 12-person,
14-day closed test before applying for production access; opted-in testers: 0.

## Listing

| Field | Prepared value |
| --- | --- |
| App | 桌游助手 |
| Package | `org.walks.gamecopilot` |
| Default language | Simplified Chinese (`zh-CN`) |
| Type and pricing | Application, free |
| Support email | `YvesSword@outlook.com` |
| Privacy URL | https://wingsword.github.io/YiGameCopilotX/store/privacy-full.html |
| English title | YiGame Tabletop Companion |

The title, short description and full description for each locale are maintained
under `fastlane/metadata/android/{zh-CN,en-US}/`. English text explicitly states
that the application interface is currently Chinese. Category, target audience
and content-rating answers still need the actual Console questionnaires; no
age rating has been invented.

Prepared images in each locale's `images/` directory:

- `icon.png`: 512 × 512, 32-bit RGBA PNG, below 1 MB. The previous icon's RGB
  pixels are unchanged; an opaque alpha channel supplies the required format.
- `featureGraphic.png`: 1024 × 500, 24-bit RGB PNG without transparency. Editable
  SVG sources are in this directory's `assets/`. Both locales were visually checked.
- `phoneScreenshots/1.png` through `3.png`: actual full-channel app screenshots,
  1080 × 1920. The English listing uses the actual Chinese interface screenshots.

Suggested accessible descriptions:

- Chinese graphic: “桌游助手的角色牌与骰子插画，介绍桌游流程、随机工具和积分记账。”
- English graphic: “A role card and dice introduce YiGame's game guides, random tools and scorekeeping.”

## Remaining publication dependencies

The app record already exists. Do not create a duplicate or modify the other
application in the account. The September 27 creation-form note is historical;
this check did not create the record or accept policy/export declarations.
Complete the existing app's store listing and content declarations using verified
facts. The saved privacy URL still needs to be included in a submitted review.

Production room HTTPS is not working at either the historical port or standard
HTTPS port in the September 27 check. The HTTP health endpoint responds normally.
Do not simply add `s` to the default URL, suppress TLS certificate verification,
or claim encrypted transmission in the Data Safety form. Obtain the working
HTTPS address and actual server log/backup retention from the server administrator.
The [deployment handoff](../../../server/deploy/HTTPS_ROLLOUT.md) is prepared.

The signed 1.6.1 Google Play AAB built successfully in
[upstream CI 36610289713](https://github.com/WingSword/YiGameCopilotX/actions/runs/36610289713)
and includes the full policy and explicit AI consent. It is a candidate artifact,
not a Play submission. The September 24 version 1.6 AAB predates these changes.
After HTTPS deployment is verified, update both clients and the generated policy,
verify room/invitation flows, and rebuild if the endpoint or disclosures change.

The [data-safety worksheet](../GOOGLE_PLAY_DATA_SAFETY_DRAFT.md) remains a draft.
Do not claim no collection, universal encryption, retention periods or completion
of production testing without evidence. New personal accounts need the applicable
closed test before production access; no testers or completed 14-day period have
been recorded for this application.

References: [Google Play image requirements](https://support.google.com/googleplay/android-developer/answer/9866151),
[User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311),
[Data Safety](https://support.google.com/googleplay/android-developer/answer/10787469),
[personal-account testing](https://support.google.com/googleplay/android-developer/answer/14151465).
