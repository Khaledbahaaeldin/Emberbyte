#!/usr/bin/env node
// Builds the Emberbyte website into site/dist (no dependencies, Node 18 or newer).
//
//   node site/tools/build-site.mjs            reads the live GitHub Releases list
//   node site/tools/build-site.mjs --offline  uses site/data/releases.json (also the fallback if GitHub is unreachable)
//
// Vercel: root directory "site", build command "node tools/build-site.mjs", output directory "dist".
// What it generates: the latest version, size and download link, the changelog from the release notes, the older
// releases, the news list (site/news/*.md), robots.txt, sitemap.xml and 404.html.
import { readFileSync, writeFileSync, mkdirSync, cpSync, readdirSync, existsSync, rmSync } from "node:fs";
import { join, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const SITE = join(dirname(fileURLToPath(import.meta.url)), "..");
const OUT = join(SITE, "dist");
const REPO = "Khaledbahaaeldin/Emberbyte";
const offline = process.argv.includes("--offline");
const siteUrl = (process.env.SITE_URL
  || (process.env.VERCEL_PROJECT_PRODUCTION_URL ? "https://" + process.env.VERCEL_PROJECT_PRODUCTION_URL : "https://emberbyte.vercel.app")).replace(/\/$/, "");

const esc = (s) => String(s).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");

// ---- a tiny, safe Markdown renderer for our own release notes and news posts ------------------------------------
function inline(text) {
  let s = esc(text);
  s = s.replace(/`([^`]+)`/g, "<code>$1</code>");
  s = s.replace(/\*\*([^*]+)\*\*/g, "<b>$1</b>");
  s = s.replace(/\[([^\]]+)\]\((https?:\/\/[^)\s]+)\)/g, '<a href="$2">$1</a>');
  return s;
}
function markdown(md, headingBase = 4) {
  const out = [];
  let list = false;
  const close = () => { if (list) { out.push("</ul>"); list = false; } };
  for (const raw of md.replace(/\r/g, "").split("\n")) {
    const line = raw.trimEnd();
    let m;
    if ((m = /^(#{1,4})\s+(.*)$/.exec(line))) { close(); const lvl = Math.min(6, headingBase + m[1].length - 1); out.push(`<h${lvl}>${inline(m[2])}</h${lvl}>`); }
    else if ((m = /^\s*[-*]\s+(.*)$/.exec(line))) { if (!list) { out.push("<ul>"); list = true; } out.push(`<li>${inline(m[1])}</li>`); }
    else if (line.trim() === "") { close(); }
    else { close(); out.push(`<p>${inline(line.trim())}</p>`); }
  }
  close();
  return out.join("\n");
}

// ---- releases --------------------------------------------------------------------------------------------------
async function loadReleases() {
  const fallback = JSON.parse(readFileSync(join(SITE, "data/releases.json"), "utf8"));
  if (offline) { return { releases: fallback, source: "snapshot (offline)" }; }
  try {
    const res = await fetch(`https://api.github.com/repos/${REPO}/releases?per_page=20`, {
      headers: { "User-Agent": "emberbyte-site-build", Accept: "application/vnd.github+json" },
      signal: AbortSignal.timeout(15000),
    });
    if (!res.ok) { throw new Error("HTTP " + res.status); }
    const live = (await res.json()).filter((r) => !r.draft);
    if (live.length === 0) { throw new Error("no published releases"); }
    return { releases: live, source: "GitHub" };
  } catch (e) {
    console.warn("GitHub releases unavailable (" + e.message + "), using the committed snapshot.");
    return { releases: fallback, source: "snapshot (fallback)" };
  }
}

const dateText = (iso) => new Date(iso).toLocaleDateString("en-GB", { day: "numeric", month: "long", year: "numeric", timeZone: "UTC" });
const mb = (bytes) => (bytes / 1e6).toFixed(1) + " MB";

function newsItems() {
  const dir = join(SITE, "news");
  if (!existsSync(dir)) { return []; }
  return readdirSync(dir).filter((f) => f.endsWith(".md")).map((f) => {
    const text = readFileSync(join(dir, f), "utf8").replace(/\r/g, "");
    const m = /^---\n([\s\S]*?)\n---\n?([\s\S]*)$/.exec(text);
    if (!m) { throw new Error(f + ": missing front matter"); }
    const meta = Object.fromEntries(m[1].split("\n").map((l) => { const i = l.indexOf(":"); return [l.slice(0, i).trim(), l.slice(i + 1).trim()]; }));
    if (!meta.title || !/^\d{4}-\d{2}-\d{2}$/.test(meta.date || "")) { throw new Error(f + ": needs title and date (YYYY-MM-DD)"); }
    return { ...meta, body: m[2].trim() };
  }).sort((a, b) => b.date.localeCompare(a.date));
}

// ---- build -----------------------------------------------------------------------------------------------------
const { releases, source } = await loadReleases();
releases.sort((a, b) => b.published_at.localeCompare(a.published_at));
const latest = releases[0];
const apk = latest.assets.find((a) => a.name.endsWith(".apk"));
const sha = latest.assets.find((a) => a.name.endsWith(".sha256"));
if (!apk || !sha) { throw new Error("latest release has no .apk and .sha256 assets"); }
const version = latest.tag_name.replace(/^v/, "");

const changelog = `<article class="card">
  <div class="ver"><h3>${esc(latest.name || "Emberbyte " + version)}</h3><time datetime="${latest.published_at.slice(0, 10)}">${dateText(latest.published_at)}</time></div>
  <div class="notes">${markdown(latest.body.replace(/^\s*#\s+[^\n]*\n/, ""), 4)}</div>
  <a href="${esc(latest.html_url)}">Open this release on GitHub</a>
</article>`;
const older = releases.slice(1).map((r) =>
  `<li><time datetime="${r.published_at.slice(0, 10)}">${dateText(r.published_at)}</time><a href="${esc(r.html_url)}">${esc(r.name || r.tag_name)}</a></li>`).join("\n");
const news = newsItems().map((n) => {
  const title = n.link ? `<a href="${esc(n.link)}">${esc(n.title)}</a>` : `<span class="t">${esc(n.title)}</span>`;
  return `<li><time datetime="${n.date}">${dateText(n.date)}</time>${title}${n.body ? `<div class="body">${markdown(n.body, 5)}</div>` : ""}</li>`;
}).join("\n");

const values = {
  site_url: siteUrl, version, apk_size: mb(apk.size), apk_name: apk.name, sha_name: sha.name,
  download_url: `https://github.com/${REPO}/releases/latest`, apk_url: apk.browser_download_url,
  changelog, older: older ? `<h4 class="older-h">Earlier releases</h4><ul class="news">${older}</ul>` : "", news,
  published: dateText(latest.published_at),
};

rmSync(OUT, { recursive: true, force: true });
mkdirSync(OUT, { recursive: true });
for (const f of ["styles.css", "app.js"]) { cpSync(join(SITE, f), join(OUT, f)); }
cpSync(join(SITE, "assets"), join(OUT, "assets"), { recursive: true });

const render = (file) => {
  let html = readFileSync(join(SITE, file), "utf8");
  html = html.replace(/\{\{(\w+)\}\}/g, (_, k) => { if (!(k in values)) { throw new Error(file + ": unknown placeholder {{" + k + "}}"); } return values[k]; });
  return html;
};
writeFileSync(join(OUT, "index.html"), render("index.html"));
writeFileSync(join(OUT, "404.html"), render("404.html"));
writeFileSync(join(OUT, "robots.txt"), `User-agent: *\nAllow: /\nSitemap: ${siteUrl}/sitemap.xml\n`);
writeFileSync(join(OUT, "sitemap.xml"),
  `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n  <url><loc>${siteUrl}/</loc><lastmod>${latest.published_at.slice(0, 10)}</lastmod></url>\n</urlset>\n`);

// every local file the pages refer to must exist in the output
for (const file of ["index.html", "404.html"]) {
  const html = readFileSync(join(OUT, file), "utf8");
  for (const m of html.matchAll(/(?:src|href)="(\/?(?:assets\/[^"#?]+|styles\.css|app\.js))"/g)) {
    if (!existsSync(join(OUT, m[1].replace(/^\//, "")))) { throw new Error(file + " refers to a missing file: " + m[1]); }
  }
}

console.log(`site built: ${releases.length} release(s) from ${source}, latest ${latest.tag_name}, ${news.split("</li>").length - 1} news item(s), url ${siteUrl}`);
