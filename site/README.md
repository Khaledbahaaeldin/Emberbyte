# Emberbyte website

A static site (no framework, no analytics, no cookies) deployed on Vercel from this folder.

## How it works
- `index.html` is a template. `tools/build-site.mjs` fills it in and writes the finished site to `dist/`:
  the latest version, size and download link, the changelog (from the latest GitHub Release notes), the earlier
  releases, the news list (`news/*.md`), `robots.txt`, `sitemap.xml` and `404.html`. It needs only Node 18+.
- If GitHub's API is unreachable the build uses `data/releases.json` (a committed snapshot), so a deploy never fails
  because of it. `--offline` forces the snapshot.
- `assets/` is generated from the app's own sources by `python tools/make-assets.py` (needs `pip install fonttools
  brotli pillow`): the Roboto Flex subset (SIL OFL, `assets/fonts/OFL.txt`), the screenshots from
  `fastlane/.../phoneScreenshots`, the 12 themes from the generated palettes, the link preview image and icons.

## Post news
Add `news/YYYY-MM-DD-title.md`:

```
---
title: Short headline
date: 2026-11-01
link: https://optional.example/link
---
Optional Markdown text.
```

Push to `main`: Vercel redeploys. A new GitHub Release rebuilds the site by itself (see below).

## Preview locally
```
node site/tools/build-site.mjs --offline
python -m http.server 5188 --directory site/dist
```

## Vercel setup (once)
1. Import the GitHub repository in Vercel. **Root Directory: `site`**. The build command, output directory and
   security headers come from `vercel.json`.
2. Settings, Git, Deploy Hooks: create a hook for `main` and save its URL as the repository secret
   `VERCEL_DEPLOY_HOOK` (GitHub, Settings, Secrets and variables, Actions). The workflow
   `.github/workflows/site-rebuild.yml` then rebuilds the site whenever a release is published or edited.
3. Optional: set `SITE_URL` (for example `https://emberbyte.app`; on Vercel the production address is detected automatically, currently `https://emberbyte-site.vercel.app`) in the Vercel environment variables so the preview
   image, canonical link and sitemap use your own domain.
