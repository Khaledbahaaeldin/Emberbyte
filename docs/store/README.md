# Store Metadata & Screenshots (`docs/store/README.md`)

This directory holds the store listing text. The listing assets themselves live where F-Droid reads them, under [`fastlane/metadata/android/en-US/`](../../fastlane/metadata/android/en-US/).

## Listing Text
- [`docs/store/store-listing.md`](store-listing.md): Title, Short Description, Full Description, Feature highlights, and Privacy declaration.
- Fastlane source: [`fastlane/metadata/android/en-US/`](../../fastlane/metadata/android/en-US/) (titles, descriptions, changelogs, icon and screenshots).

## Screenshots
Real captures of the release build from the `Violet_API_36` emulator (1080 x 2340), Dusk dark theme, English, with demo data and a clean status bar. They are in [`fastlane/metadata/android/en-US/images/phoneScreenshots/`](../../fastlane/metadata/android/en-US/images/phoneScreenshots/):

| File | Screen |
|:---|:---|
| `1_home.png` | Home: today's usage as a large number, the plan's remaining data, forecast, live speed, network split and top apps. |
| `2_apps.png` | Apps: the app with the most data as a featured card, then a ranked list with real icons. |
| `3_plans.png` | Plans: an urgency-ordered bento with a front plan, a second plan and a plan that has not started yet. |
| `4_plan_flow.png` | New plan, step 4: how to count a cycle that is already under way (the user has typed what was already used). |
| `5_themes.png` | Appearance: mode switch and the twelve themes. |
| `6_history.png` | History: the period total, the comparison with the previous period and the daily chart. |
| `7_app_detail.png` | One app: total, mobile and Wi-Fi split, daily chart and budget. |

A widgets screenshot is not included because the four widgets do not fit one launcher page. Rules for new screenshots: capture them from the release build, open every image before committing, and never change the app's behaviour to improve a picture.
