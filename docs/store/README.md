# Store Metadata & Screenshots (`docs/store/README.md`)

This directory contains the Play Store and F-Droid listing text and screenshot specifications for Emberbyte.

## Listing Text
- [`docs/store/store-listing.md`](store-listing.md): Title, Short Description, Full Description, Feature highlights, and Privacy declaration.
- Fastlane source mirrors: [`fastlane/metadata/android/en-US/`](../../fastlane/metadata/android/en-US/).

## Screenshot Specifications
The store screenshots are captured from the `Violet_API_36` phone emulator in Dusk Dark and Dusk Light modes ($1080 \times 2340$, $440\,\text{dpi}$) and stored under [`docs/store/screenshots/`](screenshots/):

| Filename | Theme | Screen / Content Description |
|:---|:---|:---|
| `01_home_dusk_dark.png` | Dusk Dark | Home screen displaying `HeroNumber` consumption metric, `WavyBar`, Forecast bento tile, Live speed tile, and Top apps today. |
| `02_home_dusk_light.png` | Dusk Light | Home screen in light mode showing dual-tone `NetworkBar`, plan progress, and recent transfer summary. |
| `03_apps_dusk_dark.png` | Dusk Dark | Apps screen featuring real application icons, proportional usage bars, and time-range filter chips. |
| `04_plans_dusk_dark.png` | Dusk Dark | Plans screen showing circular `WavyRing`, remaining quota, billing cycle details, and add-on packs. |
| `05_plan_creation_dusk_dark.png` | Dusk Dark | Plan configuration showing mid-cycle start options (From phone record, Entered usage, Start later) and PIN option. |
| `06_appearance_dusk_dark.png` | Dusk Dark | Appearance settings screen showing 12 theme preset cards, Light/Dark/System mode switcher, and AMOLED black switch. |
| `07_widgets_preview.png` | Dusk Dark | Home-screen launcher showcasing all four Glance widgets (Today 2x1, Plan 2x2, Bento 4x2, and Dashboard 4x4). |

*(Note: Real PNG captures for these filenames are generated in Task Z2 via `Violet_API_36` device captures).*
