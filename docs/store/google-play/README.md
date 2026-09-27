# Google Play submission preparation

Checked on 2026-09-27. This package is preparation, not a submitted listing or
an approved release. The developer account is verified; the console currently
contains another application, while this application's create form is unsubmitted.

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

The create form is filled with the app name, package, language, application type
and free pricing. No policy/export checkbox has been accepted and no application
has been created by this preparation. The visible creation form requires a
developer-policy declaration and a US export-compliance declaration. The latter
needs the owner's confirmation at submission time; do not infer it from a request
to publish or from another application's previous submission.

Production room HTTPS is not working at either the historical port or standard
HTTPS port in the September 27 check. The HTTP health endpoint responds normally.
Do not simply add `s` to the default URL, suppress TLS certificate verification,
or claim encrypted transmission in the Data Safety form. Obtain the working
HTTPS address and actual server log/backup retention from the server administrator.
The [deployment handoff](../../../server/deploy/HTTPS_ROLLOUT.md) is prepared.

After deployment, update both clients and the generated full privacy policy,
verify room/invitation flows, and build a signed Google Play AAB containing the
new policy and explicit AI consent. The September 24 version 1.6 AAB predates
those changes and is not the final candidate for this revised submission.

The [data-safety worksheet](../GOOGLE_PLAY_DATA_SAFETY_DRAFT.md) remains a draft.
Do not claim no collection, universal encryption, retention periods or completion
of production testing without evidence. New personal accounts need the applicable
closed test before production access; no testers or completed 14-day period have
been recorded for this application.

References: [Google Play image requirements](https://support.google.com/googleplay/android-developer/answer/9866151),
[User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311),
[Data Safety](https://support.google.com/googleplay/android-developer/answer/10787469),
[personal-account testing](https://support.google.com/googleplay/android-developer/answer/14151465).
