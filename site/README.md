# Emberbyte website

A static page (no framework, no analytics, no cookies) deployed on Vercel with this folder as the root directory.

- `index.html`, `styles.css`, `app.js`: the page. `app.js` only draws the cookie shapes, switches themes and copies text.
- `assets/`: generated files. Rebuild them from the app's own sources with `python site/tools/make-assets.py`
  (needs `pip install fonttools brotli pillow`): the Roboto Flex subset (SIL OFL, see `assets/fonts/OFL.txt`),
  the screenshots from `fastlane/.../phoneScreenshots`, and the 12 themes from the generated palettes.
- Preview locally: `python -m http.server 5188 --directory site`, then open http://localhost:5188.
