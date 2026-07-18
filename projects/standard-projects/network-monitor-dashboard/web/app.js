"use strict";

/* ------------------------------------------------------------------ */
/* Helpers                                                             */
/* ------------------------------------------------------------------ */
const $ = (id) => document.getElementById(id);

async function api(path, opts) {
  const res = await fetch(path, opts);
  if (res.status === 401) { window.location = "/login"; return null; }
  return res.json();
}

function fmtBps(bps) {
  if (!bps || bps < 1) return "0 b/s";
  const u = ["b/s", "Kb/s", "Mb/s", "Gb/s"];
  let i = 0;
  while (bps >= 1000 && i < u.length - 1) { bps /= 1000; i++; }
  return bps.toFixed(bps < 10 ? 2 : 0) + " " + u[i];
}

function fmtBytes(b) {
  if (!b) return "0 B";
  const u = ["B", "KB", "MB", "GB", "TB"];
  let i = 0;
  while (b >= 1024 && i < u.length - 1) { b /= 1024; i++; }
  return b.toFixed(b < 10 ? 2 : 1) + " " + u[i];
}

/* ------------------------------------------------------------------ */
/* Tabs                                                                */
/* ------------------------------------------------------------------ */
document.querySelectorAll(".tab").forEach((tab) => {
  tab.addEventListener("click", () => {
    document.querySelectorAll(".tab").forEach((t) => t.classList.remove("active"));
    document.querySelectorAll(".panel").forEach((p) => p.classList.remove("active"));
    tab.classList.add("active");
    $(tab.dataset.tab).classList.add("active");
  });
});

$("logout").addEventListener("click", async () => {
  await fetch("/api/logout", { method: "POST" });
  window.location = "/login";
});

/* ------------------------------------------------------------------ */
/* Canvas line chart (dessine sans librairie)                          */
/* ------------------------------------------------------------------ */
function drawLineChart(canvas, series, opts) {
  opts = opts || {};
  const ctx = canvas.getContext("2d");
  const dpr = window.devicePixelRatio || 1;
  const W = canvas.clientWidth, H = canvas.clientHeight || 220;
  canvas.width = W * dpr; canvas.height = H * dpr;
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  ctx.clearRect(0, 0, W, H);

  const padL = 56, padR = 12, padT = 12, padB = 22;
  const plotW = W - padL - padR, plotH = H - padT - padB;

  let maxV = 1;
  series.forEach((s) => s.data.forEach((v) => { if (v > maxV) maxV = v; }));
  maxV *= 1.15;

  // grille
  ctx.strokeStyle = "rgba(255,255,255,.06)";
  ctx.fillStyle = "#8ea0c4";
  ctx.font = "11px Segoe UI";
  ctx.lineWidth = 1;
  const rows = 4;
  for (let i = 0; i <= rows; i++) {
    const y = padT + (plotH / rows) * i;
    ctx.beginPath(); ctx.moveTo(padL, y); ctx.lineTo(W - padR, y); ctx.stroke();
    const val = maxV * (1 - i / rows);
    ctx.fillText(opts.fmt ? opts.fmt(val) : val.toFixed(0), 6, y + 4);
  }

  const n = Math.max(...series.map((s) => s.data.length), 2);
  const xAt = (i) => padL + (plotW * i) / (n - 1);
  const yAt = (v) => padT + plotH * (1 - v / maxV);

  series.forEach((s) => {
    if (s.data.length < 1) return;
    // aire
    const grad = ctx.createLinearGradient(0, padT, 0, padT + plotH);
    grad.addColorStop(0, s.color + "55");
    grad.addColorStop(1, s.color + "00");
    ctx.beginPath();
    ctx.moveTo(xAt(0), yAt(s.data[0]));
    s.data.forEach((v, i) => ctx.lineTo(xAt(i), yAt(v)));
    ctx.lineTo(xAt(s.data.length - 1), padT + plotH);
    ctx.lineTo(xAt(0), padT + plotH);
    ctx.closePath();
    ctx.fillStyle = grad; ctx.fill();
    // ligne
    ctx.beginPath();
    s.data.forEach((v, i) => (i ? ctx.lineTo(xAt(i), yAt(v)) : ctx.moveTo(xAt(i), yAt(v))));
    ctx.strokeStyle = s.color; ctx.lineWidth = 2; ctx.stroke();
  });
}

/* ------------------------------------------------------------------ */
/* Data loaders                                                        */
/* ------------------------------------------------------------------ */
async function loadOverview() {
  const d = await api("/api/overview");
  if (!d) return;
  $("ov-online").textContent = d.devices_online;
  $("ov-total").textContent = "sur " + d.devices_total + " detectes";
  $("ov-in").textContent = fmtBps(d.cur_in_bps);
  $("ov-out").textContent = fmtBps(d.cur_out_bps);
  $("ov-hosts").textContent = d.hosts_tracked;
  $("clock").textContent = d.server_time;
  $("net-local").textContent = d.net.local_ip || "-";
  $("net-gw").textContent = d.net.gateway || "-";
  $("net-subnet").textContent = d.net.subnet || "-";
  $("net-public").textContent = d.public_ip || "-";
  $("net-loc").textContent = d.location || "...";
  $("net-isp").textContent = d.isp || "...";
  updateAlertBadges({ critical: d.alerts_critical || 0, warning: d.alerts_warning || 0, info: 0 });
}

async function loadTraffic() {
  const d = await api("/api/traffic");
  if (!d) return;
  const inData = d.history.map((h) => h.in_bps);
  const outData = d.history.map((h) => h.out_bps);
  const series = [
    { data: inData, color: "#34d399" },
    { data: outData, color: "#fb923c" },
  ];
  if ($("overview").classList.contains("active"))
    drawLineChart($("ovChart"), series, { fmt: fmtBps });
  if ($("traffic").classList.contains("active"))
    drawLineChart($("trChart"), series, { fmt: fmtBps });

  $("tr-total-in").textContent = fmtBytes(d.total_in);
  $("tr-total-out").textContent = fmtBytes(d.total_out);
  $("tr-peak-in").textContent = fmtBps(Math.max(0, ...inData));
  $("tr-peak-out").textContent = fmtBps(Math.max(0, ...outData));
}

async function loadDevices() {
  const d = await api("/api/devices");
  if (!d) return;
  const tbody = $("devTable").querySelector("tbody");
  $("dev-count").textContent = d.devices.length;
  tbody.innerHTML = d.devices.map((dev) => {
    const st = dev.online
      ? '<span class="status on"><i></i>en ligne</span>'
      : '<span class="status off"><i></i>hors ligne</span>';
    let role = "";
    if (dev.is_self) role = '<span class="tag self">cette machine</span>';
    else if (dev.is_gateway) role = '<span class="tag gw">passerelle</span>';
    const blockBtn = (dev.is_self || dev.is_gateway)
      ? ""
      : `<button class="btn-block" data-mac="${dev.mac}" data-ip="${dev.ip}">Bloquer</button>`;
    return `<tr>
      <td>${st}</td>
      <td><span class="dev-type"><span class="dev-ico">${dev.icon || "🔌"}</span>${dev.type || "Appareil"}</span></td>
      <td class="mono">${dev.ip}</td>
      <td>${dev.hostname || "<span style='color:#5b6a8c'>—</span>"}</td>
      <td>${dev.vendor}</td>
      <td class="mono">${dev.mac}</td>
      <td>${dev.online ? dev.connected_for_h : "—"}</td>
      <td>${dev.online ? "à l'instant" : dev.last_seen_h}</td>
      <td>${role}</td>
      <td>${blockBtn}</td>
    </tr>`;
  }).join("");

  tbody.querySelectorAll(".btn-block").forEach((b) => {
    b.addEventListener("click", async () => {
      const r = await api("/api/block", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ mac: b.dataset.mac, ip: b.dataset.ip }),
      });
      alert(r.message || "Action envoyee.");
    });
  });
}

async function loadFlows() {
  const d = await api("/api/connections");
  if (!d) return;
  const tbody = $("flowTable").querySelector("tbody");
  $("flow-count").textContent = d.connections.length;
  const maxSec = Math.max(1, ...d.connections.map((c) => c.seconds));
  tbody.innerHTML = d.connections.map((c) => {
    const dot = c.active
      ? '<span class="status on"><i></i></span>'
      : '<span class="status off"><i></i></span>';
    const procs = c.procs.length
      ? `<div class="chips">${c.procs.map((p) => `<span class="chip">${p}</span>`).join("")}</div>`
      : "—";
    const ports = c.ports.map((p) => `<span class="chip">${p}</span>`).join(" ");
    const pct = Math.round((c.seconds / maxSec) * 100);
    // adresse technique secondaire (DNS inverse si different de l'IP)
    const tech = (c.host && c.host !== c.ip)
      ? `${c.host}<br><span class="mono dim">${c.ip}</span>`
      : `<span class="mono">${c.ip}</span>`;
    return `<tr>
      <td>${dot}</td>
      <td><span class="svc-name">${c.service}</span></td>
      <td>${procs}</td>
      <td class="tech-cell">${tech}</td>
      <td><div class="chips">${ports}</div></td>
      <td>${c.seconds_h}</td>
      <td><div class="share-bar"><div class="share-fill" style="width:${pct}%"></div></div></td>
    </tr>`;
  }).join("");
}

/* ------------------------------------------------------------------ */
/* Map (projection equirectangulaire, autonome)                        */
/* ------------------------------------------------------------------ */
function renderMap(geo) {
  const wrap = $("mapWrap");
  const W = wrap.clientWidth, H = wrap.clientHeight;
  const lat = geo.lat, lon = geo.lon;
  const x = ((lon + 180) / 360) * W;
  const y = ((90 - lat) / 180) * H;
  // grille lat/lon + marqueur
  let grid = "";
  for (let i = 1; i < 6; i++) {
    const gx = (W / 6) * i;
    grid += `<line x1="${gx}" y1="0" x2="${gx}" y2="${H}" stroke="rgba(79,140,255,.10)"/>`;
  }
  for (let i = 1; i < 4; i++) {
    const gy = (H / 4) * i;
    grid += `<line x1="0" y1="${gy}" x2="${W}" y2="${gy}" stroke="rgba(79,140,255,.10)"/>`;
  }
  wrap.innerHTML = `
    <svg width="${W}" height="${H}" style="position:absolute;inset:0">
      <defs>
        <radialGradient id="glow"><stop offset="0%" stop-color="#4f8cff" stop-opacity=".9"/>
        <stop offset="100%" stop-color="#4f8cff" stop-opacity="0"/></radialGradient>
      </defs>
      <rect width="${W}" height="${H}" fill="#0e1730"/>
      ${grid}
      <circle cx="${x}" cy="${y}" r="34" fill="url(#glow)">
        <animate attributeName="r" values="18;40;18" dur="2.4s" repeatCount="indefinite"/>
        <animate attributeName="opacity" values=".9;.2;.9" dur="2.4s" repeatCount="indefinite"/>
      </circle>
      <circle cx="${x}" cy="${y}" r="6" fill="#4f8cff" stroke="#fff" stroke-width="2"/>
      <text x="${Math.min(x + 12, W - 120)}" y="${y - 10}" fill="#cfe0ff" font-size="12" font-family="Segoe UI">
        ${(geo.city || "")} ${geo.lat ? geo.lat.toFixed(2) : ""}, ${geo.lon ? geo.lon.toFixed(2) : ""}
      </text>
    </svg>`;
}

function renderTopology(devices) {
  const wrap = $("topoWrap");
  const W = wrap.clientWidth, H = wrap.clientHeight;
  const cx = W / 2, cy = H / 2;
  const online = devices.filter((d) => d.online);
  const gw = online.find((d) => d.is_gateway);
  const nodes = online.filter((d) => !d.is_gateway);
  $("topo-count").textContent = online.length;

  const R = Math.min(W, H) * 0.36;
  let links = "", dots = "";
  nodes.forEach((d, i) => {
    const ang = (2 * Math.PI * i) / Math.max(nodes.length, 1) - Math.PI / 2;
    const x = cx + R * Math.cos(ang), y = cy + R * Math.sin(ang);
    const color = d.is_self ? "#34d399" : "#4f8cff";
    links += `<line x1="${cx}" y1="${cy}" x2="${x}" y2="${y}" stroke="rgba(79,140,255,.25)" stroke-width="1.5"/>`;
    const label = d.hostname || d.ip;
    const short = label.length > 18 ? label.slice(0, 17) + "…" : label;
    dots += `<g class="topo-node" data-ip="${d.ip}" style="cursor:pointer">
      <circle cx="${x}" cy="${y}" r="22" fill="#16203a" stroke="${color}" stroke-width="2"/>
      <text x="${x}" y="${y + 6}" text-anchor="middle" font-size="18">${d.icon || "🔌"}</text>
      <text x="${x}" y="${y + 40}" text-anchor="middle" fill="#cfe0ff" font-size="11" font-family="Segoe UI">${short}</text>
      <text x="${x}" y="${y + 54}" text-anchor="middle" fill="#8ea0c4" font-size="10" font-family="Segoe UI">${d.ip}</text>
    </g>`;
  });

  const gwLabel = gw ? (gw.hostname || gw.ip) : "Routeur";
  wrap.innerHTML = `
    <svg width="${W}" height="${H}" style="position:absolute;inset:0">
      ${links}
      <circle cx="${cx}" cy="${cy}" r="40" fill="#1c2848" stroke="#7aa8ff" stroke-width="2.5"/>
      <text x="${cx}" y="${cy + 8}" text-anchor="middle" font-size="26">🌐</text>
      <text x="${cx}" y="${cy + 60}" text-anchor="middle" fill="#9dc0ff" font-size="12" font-family="Segoe UI" font-weight="600">${gwLabel}</text>
      ${dots}
    </svg>`;

  wrap.querySelectorAll(".topo-node").forEach((n) => {
    n.addEventListener("click", () => {
      document.querySelector('.tab[data-tab="devices"]').click();
    });
  });
}

async function loadMap() {
  const dev = await api("/api/devices");
  if (dev) renderTopology(dev.devices);
  const geo = await api("/api/geo");
  if (!geo || !geo.loaded) return;
  $("map-ip").textContent = geo.query || "-";
  $("map-city").textContent = geo.city || "-";
  $("map-region").textContent = geo.regionName || "-";
  $("map-country").textContent = geo.country || "-";
  $("map-coords").textContent = (geo.lat && geo.lon) ? `${geo.lat}, ${geo.lon}` : "-";
  $("map-isp").textContent = geo.isp || "-";
  if (geo.lat != null && geo.lon != null) renderMap(geo);
}

/* ------------------------------------------------------------------ */
/* Speedtest                                                           */
/* ------------------------------------------------------------------ */
$("sp-run").addEventListener("click", async () => {
  const btn = $("sp-run");
  const status = $("sp-status");
  const fields = ["sp-down", "sp-up", "sp-ping", "sp-jitter"];
  btn.disabled = true;
  btn.textContent = "Test en cours…";
  status.textContent = "Mesure du débit (10-20 s)…";
  fields.forEach((f) => { $(f).textContent = "…"; $(f).classList.add("testing"); });
  try {
    const d = await api("/api/speedtest");
    if (!d) return;
    $("sp-down").textContent = d.download_mbps || 0;
    $("sp-up").textContent = d.upload_mbps || 0;
    $("sp-ping").textContent = d.ping_ms != null ? d.ping_ms : "—";
    $("sp-jitter").textContent = d.jitter_ms != null ? d.jitter_ms : "—";
    status.textContent = d.error
      ? "Test partiel — " + d.error
      : `Terminé à ${d.ts} · serveur ${d.server}`;
  } catch (e) {
    status.textContent = "Échec du test (connexion Internet ?).";
  } finally {
    fields.forEach((f) => $(f).classList.remove("testing"));
    btn.disabled = false;
    btn.textContent = "Relancer le test";
  }
});

/* ------------------------------------------------------------------ */
/* Alertes & Urgences                                                  */
/* ------------------------------------------------------------------ */
let alertFilter = "all";
const SEV_LABEL = { critical: "critique", warning: "avertissement", info: "info" };

function alertCard(a) {
  const origin = (a.origin && (a.origin.country || a.origin.city))
    ? `<div class="alert-origin">Origine : <b>${[a.origin.city, a.origin.country].filter(Boolean).join(", ")}</b>${a.origin.isp ? " — " + a.origin.isp : ""}</div>`
    : "";
  const count = a.count > 1 ? `<span class="alert-count">×${a.count}</span>` : "";
  return `<div class="alert-item ${a.severity}">
    <div class="alert-head">
      <span class="sev-badge ${a.severity}">${SEV_LABEL[a.severity]}</span>
      <span class="alert-title">${a.title}</span>
      ${count}
      <span class="alert-meta">${a.ts_h}${a.source ? " · " + a.source : ""}</span>
    </div>
    <p class="alert-detail">${a.detail}</p>
    ${origin}
    <div class="alert-defense"><b>Se defendre :</b> ${a.defense}</div>
  </div>`;
}

function updateAlertBadges(counts) {
  const ab = $("tab-alerts-badge"), ub = $("tab-urgent-badge");
  const w = (counts.warning || 0) + (counts.info || 0);
  const c = counts.critical || 0;
  ab.textContent = w; ab.classList.toggle("hidden", w === 0);
  ub.textContent = c; ub.classList.toggle("hidden", c === 0);
}

async function loadAlerts() {
  const d = await api("/api/alerts");
  if (!d) return;
  $("al-crit").textContent = d.counts.critical || 0;
  $("al-warn").textContent = d.counts.warning || 0;
  $("al-info").textContent = d.counts.info || 0;
  $("al-total").textContent = d.total;
  updateAlertBadges(d.counts);

  const list = alertFilter === "all"
    ? d.alerts
    : d.alerts.filter((a) => a.severity === alertFilter);
  $("alertList").innerHTML = list.length
    ? list.map(alertCard).join("")
    : '<div class="alert-empty">Aucune alerte pour ce filtre.</div>';
}

async function loadUrgent() {
  const d = await api("/api/alerts");
  if (!d) return;
  updateAlertBadges(d.counts);
  const crit = d.alerts.filter((a) => a.severity === "critical");
  const banner = $("urgentBanner");
  if (crit.length) {
    banner.className = "urgent-banner alarm";
    banner.innerHTML = `<div class="ub-icon">⚠</div><div>
      <div class="ub-title">${crit.length} urgence(s) critique(s) en cours</div>
      <div class="ub-sub">Des attaques ou acces non autorises ont ete detectes. Traitez-les ci-dessous.</div></div>`;
    $("urgentList").innerHTML = crit.map(alertCard).join("");
  } else {
    banner.className = "urgent-banner ok";
    banner.innerHTML = `<div class="ub-icon">✓</div><div>
      <div class="ub-title">Aucune urgence en cours</div>
      <div class="ub-sub">Aucune attaque critique detectee. La surveillance est active tant que l'application est ouverte.</div></div>`;
    $("urgentList").innerHTML = "";
  }
}

document.querySelectorAll(".filt").forEach((b) => {
  b.addEventListener("click", () => {
    document.querySelectorAll(".filt").forEach((x) => x.classList.remove("active"));
    b.classList.add("active");
    alertFilter = b.dataset.filt;
    loadAlerts();
  });
});

/* ------------------------------------------------------------------ */
/* Change password                                                     */
/* ------------------------------------------------------------------ */
$("pwForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const msg = $("pw-msg");
  msg.className = "form-msg";
  const cur = $("pw-current").value;
  const nw = $("pw-new").value;
  const cf = $("pw-confirm").value;
  if (nw !== cf) { msg.textContent = "La confirmation ne correspond pas."; msg.classList.add("err"); return; }
  const r = await api("/api/change-password", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ current: cur, new: nw }),
  });
  if (r && r.ok) {
    msg.textContent = "Mot de passe modifie avec succes.";
    msg.classList.add("ok");
    $("pwForm").reset();
  } else {
    msg.textContent = (r && r.error) || "Erreur.";
    msg.classList.add("err");
  }
});

/* ------------------------------------------------------------------ */
/* Arret de l'application                                              */
/* ------------------------------------------------------------------ */
$("shutdownBtn").addEventListener("click", async () => {
  if (!confirm("Arrêter l'application de monitoring ?")) return;
  const msg = $("shutdownMsg");
  msg.className = "form-msg";
  try {
    await fetch("/api/shutdown", { method: "POST" });
  } catch (e) { /* le serveur se coupe, l'erreur reseau est normale */ }
  if (window.__stopRefresh) window.__stopRefresh();
  document.body.innerHTML =
    '<div style="display:flex;align-items:center;justify-content:center;height:100vh;flex-direction:column;gap:12px;color:#e7ecf7;font-family:Segoe UI">' +
    '<div style="font-size:42px">⏻</div>' +
    '<div style="font-size:20px;font-weight:600">Application arrêtée</div>' +
    "<div style=\"color:#8ea0c4\">Le serveur est coupé. Vous pouvez fermer cet onglet.</div></div>";
});

/* ------------------------------------------------------------------ */
/* Boucles de rafraichissement                                         */
/* ------------------------------------------------------------------ */
function refreshAll() {
  loadOverview();
  loadTraffic();
  const active = document.querySelector(".panel.active").id;
  if (active === "devices") loadDevices();
  if (active === "flows") loadFlows();
  if (active === "map") loadMap();
  if (active === "alerts") loadAlerts();
  if (active === "urgent") loadUrgent();
}

document.querySelectorAll(".tab").forEach((tab) => {
  tab.addEventListener("click", () => {
    const id = tab.dataset.tab;
    if (id === "devices") loadDevices();
    if (id === "flows") loadFlows();
    if (id === "map") loadMap();
    if (id === "traffic") loadTraffic();
    if (id === "alerts") loadAlerts();
    if (id === "urgent") loadUrgent();
  });
});

// premier chargement + intervalles
loadOverview();
loadTraffic();
loadDevices();
const _iv1 = setInterval(refreshAll, 3000);
const _iv2 = setInterval(loadDevices, 15000);
window.__stopRefresh = () => { clearInterval(_iv1); clearInterval(_iv2); };
window.addEventListener("resize", loadTraffic);
