// Emberbyte website: cookie shapes, theme switcher, light/dark mode and copy buttons. No network, no tracking.
(function () {
  "use strict";
  var root = document.documentElement;
  var themes = window.EMBERBYTE_THEMES || [];
  var state = { id: "dusk", mode: null };

  function store(key, value) { try { if (value === undefined) { return localStorage.getItem(key); } localStorage.setItem(key, value); } catch (e) { return null; } }

  // The scalloped "cookie" shape of the app, drawn as an SVG path.
  function cookiePath(bumps, depth) {
    var pts = [], n = 180, i, a, r;
    for (i = 0; i < n; i++) {
      a = (i / n) * Math.PI * 2;
      r = 44 + depth * Math.cos(a * bumps);
      pts.push((r * Math.cos(a)).toFixed(2) + "," + (r * Math.sin(a)).toFixed(2));
    }
    return "M" + pts.join("L") + "Z";
  }
  document.querySelectorAll("svg.cookie").forEach(function (svg) {
    var bumps = parseInt(svg.getAttribute("data-bumps") || "9", 10);
    svg.setAttribute("viewBox", "-50 -50 100 100");
    svg.setAttribute("aria-hidden", "true");
    svg.innerHTML = '<path d="' + cookiePath(bumps, 3.6) + '" fill="currentColor"/>';
  });

  function effectiveMode() {
    if (state.mode) { return state.mode; }
    return window.matchMedia && window.matchMedia("(prefers-color-scheme: light)").matches ? "light" : "dark";
  }

  function apply() {
    var t = themes.filter(function (x) { return x.id === state.id; })[0] || themes[0];
    if (!t) { return; }
    var mode = effectiveMode();
    var p = t[mode];
    var map = { background: "--bg", surface: "--surface", surface2: "--surface2", text: "--text", muted: "--muted", primary: "--primary",
      onPrimary: "--on-primary", accent: "--accent", onAccent: "--on-accent", warn: "--warn", danger: "--danger", track: "--track" };
    Object.keys(map).forEach(function (k) { root.style.setProperty(map[k], p[k]); });
    root.setAttribute("data-mode", mode);
    var meta = document.querySelector('meta[name="theme-color"]');
    if (meta) { meta.setAttribute("content", p.background); }
    document.querySelectorAll(".sw").forEach(function (b) { b.setAttribute("aria-pressed", String(b.getAttribute("data-id") === t.id)); });
    var label = document.getElementById("theme-name");
    if (label) { label.textContent = t.name + " · " + (mode === "dark" ? "dark" : "light"); }
    var modeBtn = document.getElementById("mode");
    if (modeBtn) { modeBtn.textContent = mode === "dark" ? "☾" : "☀"; modeBtn.setAttribute("aria-label", mode === "dark" ? "Switch to light mode" : "Switch to dark mode"); }
  }

  var sw = document.getElementById("swatches");
  if (sw) {
    themes.forEach(function (t) {
      var b = document.createElement("button");
      b.className = "sw"; b.type = "button"; b.setAttribute("data-id", t.id); b.setAttribute("aria-label", t.name + " theme");
      b.title = t.name;
      b.style.background = t.dark.primary; b.style.setProperty("--sw1", t.dark.surface); b.style.setProperty("--sw2", t.dark.accent);
      b.addEventListener("click", function () { state.id = t.id; store("emberbyte-theme", t.id); apply(); });
      sw.appendChild(b);
    });
  }

  var modeBtn = document.getElementById("mode");
  if (modeBtn) {
    modeBtn.addEventListener("click", function () { state.mode = effectiveMode() === "dark" ? "light" : "dark"; store("emberbyte-mode", state.mode); apply(); });
  }

  var savedTheme = store("emberbyte-theme"), savedMode = store("emberbyte-mode");
  if (savedTheme && themes.some(function (t) { return t.id === savedTheme; })) { state.id = savedTheme; }
  if (savedMode === "light" || savedMode === "dark") { state.mode = savedMode; }
  apply();

  document.querySelectorAll("[data-copy]").forEach(function (btn) {
    btn.addEventListener("click", function () {
      var el = document.getElementById(btn.getAttribute("data-copy"));
      if (!el || !navigator.clipboard) { return; }
      navigator.clipboard.writeText(el.textContent.trim()).then(function () {
        var old = btn.textContent; btn.textContent = "Copied"; setTimeout(function () { btn.textContent = old; }, 1600);
      });
    });
  });
})();
