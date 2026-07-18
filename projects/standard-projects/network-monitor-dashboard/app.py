#!/usr/bin/env python3
"""
LAN Monitor - tableau de bord de monitoring du reseau local (usage personnel).

100% standard library : aucune dependance a installer.
Lancer :  python app.py   puis ouvrir http://127.0.0.1:8787

Ce que fait l'outil (monitoring legitime de VOTRE reseau) :
  - decouverte des appareils du LAN (IP privee, MAC, constructeur, nom, en ligne, duree)
  - IP publique geolocalisee
  - trafic entrant/sortant de cette machine (bande passante temps reel + historique)
  - connexions actives regroupees par hote distant + temps passe par destination
  - login admin (admin/admin par defaut), mot de passe modifiable

Ce que l'outil NE fait PAS (volontairement) : capter les mots de passe / identifiants
/ navigation des autres personnes. C'est du vol d'identifiants et de l'ecoute illegale,
impossible sans casser le HTTPS des autres. Hors perimetre.
"""

import os
import re
import io
import csv
import json
import time
import socket
import hashlib
import secrets
import threading
import subprocess
import urllib.request
from datetime import datetime
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse, parse_qs

# --------------------------------------------------------------------------- #
# Chemins & constantes
# --------------------------------------------------------------------------- #
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
WEB_DIR = os.path.join(BASE_DIR, "web")
DATA_DIR = os.path.join(BASE_DIR, "data")
CONFIG_PATH = os.path.join(DATA_DIR, "config.json")
DEVICES_PATH = os.path.join(DATA_DIR, "devices.json")

HOST = "127.0.0.1"
PORT = 8787

os.makedirs(DATA_DIR, exist_ok=True)

IS_WINDOWS = os.name == "nt"
SUBPROCESS_FLAGS = 0x08000000 if IS_WINDOWS else 0  # CREATE_NO_WINDOW

# --------------------------------------------------------------------------- #
# Utilitaires systeme
# --------------------------------------------------------------------------- #
def run_cmd(args, timeout=15):
    """Execute une commande et renvoie sa sortie texte (decode tolerant)."""
    try:
        out = subprocess.run(
            args,
            capture_output=True,
            timeout=timeout,
            creationflags=SUBPROCESS_FLAGS,
        )
        raw = out.stdout or b""
        return raw.decode("utf-8", errors="replace")
    except Exception:
        return ""


def now_ts():
    return time.time()


def human_duration(seconds):
    seconds = int(max(0, seconds))
    d, seconds = divmod(seconds, 86400)
    h, seconds = divmod(seconds, 3600)
    m, s = divmod(seconds, 60)
    if d:
        return f"{d}j {h}h {m}m"
    if h:
        return f"{h}h {m}m"
    if m:
        return f"{m}m {s}s"
    return f"{s}s"


# --------------------------------------------------------------------------- #
# Config & authentification
# --------------------------------------------------------------------------- #
def hash_password(password, salt):
    return hashlib.sha256((salt + password).encode("utf-8")).hexdigest()


def load_config():
    if not os.path.exists(CONFIG_PATH):
        salt = secrets.token_hex(16)
        cfg = {
            "username": "admin",
            "salt": salt,
            "password_hash": hash_password("admin", salt),
            "created": datetime.now().isoformat(timespec="seconds"),
        }
        save_config(cfg)
        return cfg
    with open(CONFIG_PATH, "r", encoding="utf-8") as fh:
        return json.load(fh)


def save_config(cfg):
    with open(CONFIG_PATH, "w", encoding="utf-8") as fh:
        json.dump(cfg, fh, indent=2)


CONFIG = load_config()
SESSIONS = {}  # token -> expiry timestamp
SESSION_TTL = 8 * 3600


def check_credentials(username, password):
    if username != CONFIG.get("username", "admin"):
        return False
    return hash_password(password, CONFIG["salt"]) == CONFIG["password_hash"]


def new_session():
    token = secrets.token_hex(24)
    SESSIONS[token] = now_ts() + SESSION_TTL
    return token


def session_valid(token):
    if not token:
        return False
    exp = SESSIONS.get(token)
    if not exp:
        return False
    if exp < now_ts():
        SESSIONS.pop(token, None)
        return False
    return True


# --------------------------------------------------------------------------- #
# Constructeurs (OUI) - petite table pour les prefixes MAC courants
# --------------------------------------------------------------------------- #
OUI = {
    "00:1A:11": "Google", "3C:5A:B4": "Google", "F4:F5:E8": "Google",
    "00:50:56": "VMware", "00:0C:29": "VMware", "08:00:27": "VirtualBox",
    "DC:A6:32": "Raspberry Pi", "B8:27:EB": "Raspberry Pi", "E4:5F:01": "Raspberry Pi",
    "FC:FB:FB": "Cisco", "00:1B:44": "Cisco",
    "00:24:D7": "Intel", "3C:97:0E": "Intel", "A4:C3:F0": "Intel", "94:65:9C": "Intel",
    "AC:DE:48": "Apple", "F0:18:98": "Apple", "A4:83:E7": "Apple", "3C:15:C2": "Apple",
    "40:B0:76": "Samsung", "5C:0A:5B": "Samsung", "8C:77:12": "Samsung",
    "50:8F:4C": "Xiaomi", "64:09:80": "Xiaomi", "F8:A4:5F": "Xiaomi",
    "D8:0D:17": "TP-Link", "50:C7:BF": "TP-Link", "AC:84:C6": "TP-Link",
    "00:1D:0F": "Huawei", "10:47:80": "Huawei", "48:AD:08": "Huawei",
    "E0:CB:1D": "Nokia", "00:E0:03": "Nokia",
    "00:15:5D": "Microsoft/Hyper-V", "60:45:BD": "Microsoft",
}


def vendor_from_mac(mac):
    if not mac:
        return "Inconnu"
    prefix = mac.upper().replace("-", ":")[:8]
    return OUI.get(prefix, "Inconnu")


# --------------------------------------------------------------------------- #
# Reseau : IP locale, passerelle, sous-reseau
# --------------------------------------------------------------------------- #
def local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"


def gateway_ip():
    txt = run_cmd(["ipconfig"] if IS_WINDOWS else ["ip", "route"])
    if IS_WINDOWS:
        m = re.findall(r"(?:Passerelle par d.faut|Default Gateway)[ .]*:\s*([0-9.]+)", txt)
        for g in m:
            if g and g != "0.0.0.0":
                return g
    else:
        m = re.search(r"default via ([0-9.]+)", txt)
        if m:
            return m.group(1)
    ip = local_ip()
    return ip.rsplit(".", 1)[0] + ".1"


MAC_RE = re.compile(r"([0-9a-fA-F]{2}[:-]){5}[0-9a-fA-F]{2}")
IP_RE = re.compile(r"(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})")


def parse_arp():
    """Renvoie {ip: mac} depuis la table ARP du systeme."""
    txt = run_cmd(["arp", "-a"])
    result = {}
    for line in txt.splitlines():
        ip_m = IP_RE.search(line)
        mac_m = MAC_RE.search(line)
        if ip_m and mac_m:
            ip = ip_m.group(1)
            mac = mac_m.group(0).replace("-", ":").lower()
            if ip.endswith(".255") or ip.startswith("224.") or ip.startswith("239."):
                continue
            if mac in ("ff:ff:ff:ff:ff:ff", "00:00:00:00:00:00"):
                continue
            result[ip] = mac
    return result


def ping_host(ip):
    if IS_WINDOWS:
        args = ["ping", "-n", "1", "-w", "400", ip]
    else:
        args = ["ping", "-c", "1", "-W", "1", ip]
    out = run_cmd(args, timeout=3)
    return ("TTL=" in out) or ("ttl=" in out)


def ping_sweep(subnet_prefix):
    """Ping /24 en parallele pour peupler la table ARP."""
    threads = []
    sem = threading.Semaphore(64)

    def worker(addr):
        with sem:
            ping_host(addr)

    for i in range(1, 255):
        addr = f"{subnet_prefix}.{i}"
        t = threading.Thread(target=worker, args=(addr,), daemon=True)
        t.start()
        threads.append(t)
    for t in threads:
        t.join(timeout=5)


def resolve_hostname(ip):
    try:
        return socket.gethostbyaddr(ip)[0]
    except Exception:
        return ""


# --------------------------------------------------------------------------- #
# Etat partage
# --------------------------------------------------------------------------- #
STATE_LOCK = threading.Lock()

DEVICES = {}   # mac -> device dict
TRAFFIC = {    # historique de bande passante
    "history": [],           # liste de {t, in_bps, out_bps}
    "total_in": 0,
    "total_out": 0,
    "last_in": None,
    "last_out": None,
    "last_t": None,
}
CONN_HOSTS = {}  # remote_ip -> {host, ports, procs, first_seen, last_seen, seconds}
GEO = {"loaded": False}
NETINFO = {"local_ip": local_ip(), "gateway": "", "subnet": ""}

ALERTS = []            # liste d'alertes (plus recentes en tete), capee
ALERT_INDEX = {}       # cle dedup -> alerte (pour incrementer count)
SCAN_TRACK = {}        # ip externe -> {ports:set, first, last}  (detection de scan)
GEO_CACHE = {}         # ip -> {country, city} (origine des attaques)
FIRST_SCAN_DONE = {"v": False}
EXPOSED_SEEN = set()   # ports en ecoute deja signales
ALERT_SEQ = {"n": 0}

# Ports sensibles souvent cibles par les attaques
SENSITIVE_PORTS = {
    21: "FTP", 22: "SSH", 23: "Telnet", 25: "SMTP", 135: "RPC Windows",
    137: "NetBIOS", 138: "NetBIOS", 139: "NetBIOS", 445: "SMB (partage Windows)",
    1433: "SQL Server", 1521: "Oracle", 3306: "MySQL", 3389: "Bureau a distance (RDP)",
    5432: "PostgreSQL", 5900: "VNC", 5985: "WinRM", 6379: "Redis", 27017: "MongoDB",
}


def load_devices():
    if os.path.exists(DEVICES_PATH):
        try:
            with open(DEVICES_PATH, "r", encoding="utf-8") as fh:
                data = json.load(fh)
            for mac, dev in data.items():
                dev["online"] = False
                DEVICES[mac] = dev
        except Exception:
            pass


def save_devices():
    try:
        with STATE_LOCK:
            snapshot = dict(DEVICES)
        with open(DEVICES_PATH, "w", encoding="utf-8") as fh:
            json.dump(snapshot, fh, indent=2)
    except Exception:
        pass


# --------------------------------------------------------------------------- #
# Threads de collecte
# --------------------------------------------------------------------------- #
def scanner_loop():
    ip = local_ip()
    subnet_prefix = ip.rsplit(".", 1)[0]
    NETINFO["local_ip"] = ip
    NETINFO["subnet"] = subnet_prefix + ".0/24"
    NETINFO["gateway"] = gateway_ip()

    while True:
        try:
            ping_sweep(subnet_prefix)
            arp = parse_arp()
            seen_now = set()
            t = now_ts()
            for dip, mac in arp.items():
                seen_now.add(mac)
                with STATE_LOCK:
                    dev = DEVICES.get(mac)
                    if not dev:
                        dev = {
                            "mac": mac,
                            "ip": dip,
                            "vendor": vendor_from_mac(mac),
                            "hostname": "",
                            "first_seen": t,
                            "last_seen": t,
                            "online": True,
                            "is_gateway": dip == NETINFO["gateway"],
                            "is_self": dip == ip,
                        }
                        DEVICES[mac] = dev
                        # Nouvel appareil apparu apres le 1er scan complet
                        if FIRST_SCAN_DONE["v"] and dip != ip and dip != NETINFO["gateway"]:
                            add_alert(
                                "warning", "newdevice", mac,
                                f"Nouvel appareil sur le reseau : {dip}",
                                f"Un appareil inconnu ({dip}, MAC {mac}, "
                                f"{vendor_from_mac(mac)}) a rejoint votre WiFi. Si vous "
                                f"pensiez etre seul, quelqu'un utilise peut-etre votre reseau.",
                                "Verifiez la liste des appareils sur votre routeur. "
                                "Changez le mot de passe WiFi (WPA2/WPA3 fort), et "
                                "activez le filtrage par adresse MAC si possible.",
                            )
                    else:
                        dev["ip"] = dip
                        dev["last_seen"] = t
                        dev["online"] = True
                        dev["is_gateway"] = dip == NETINFO["gateway"]
                        dev["is_self"] = dip == ip
                        if not dev.get("vendor") or dev["vendor"] == "Inconnu":
                            dev["vendor"] = vendor_from_mac(mac)
                # resolution de nom hors verrou
                if not dev.get("hostname"):
                    hn = resolve_hostname(dip)
                    if hn:
                        with STATE_LOCK:
                            dev["hostname"] = hn

            # marquer hors ligne ceux non vus
            with STATE_LOCK:
                for mac, dev in DEVICES.items():
                    if mac not in seen_now:
                        dev["online"] = False

            save_devices()
            FIRST_SCAN_DONE["v"] = True
        except Exception:
            pass
        time.sleep(30)


def read_netstat_bytes():
    """Renvoie (bytes_in, bytes_out) cumulatifs de l'interface."""
    txt = run_cmd(["netstat", "-e"])
    for line in txt.splitlines():
        low = line.strip().lower()
        if low.startswith("bytes") or low.startswith("octets"):
            nums = re.findall(r"\d+", line)
            if len(nums) >= 2:
                return int(nums[0]), int(nums[1])
    return None, None


def traffic_loop():
    while True:
        try:
            bin_, bout = read_netstat_bytes()
            t = now_ts()
            if bin_ is not None:
                with STATE_LOCK:
                    last_in = TRAFFIC["last_in"]
                    last_out = TRAFFIC["last_out"]
                    last_t = TRAFFIC["last_t"]
                    if last_in is not None and last_t is not None and t > last_t:
                        dt = t - last_t
                        # gestion d'un eventuel reset de compteur
                        din = max(0, bin_ - last_in)
                        dout = max(0, bout - last_out)
                        in_bps = din * 8 / dt
                        out_bps = dout * 8 / dt
                        TRAFFIC["history"].append(
                            {"t": t, "in_bps": in_bps, "out_bps": out_bps}
                        )
                        if len(TRAFFIC["history"]) > 120:
                            TRAFFIC["history"] = TRAFFIC["history"][-120:]
                    TRAFFIC["last_in"] = bin_
                    TRAFFIC["last_out"] = bout
                    TRAFFIC["last_t"] = t
                    TRAFFIC["total_in"] = bin_
                    TRAFFIC["total_out"] = bout
        except Exception:
            pass
        time.sleep(2)


def get_process_map():
    """PID -> nom de processus (Windows tasklist)."""
    pmap = {}
    if not IS_WINDOWS:
        return pmap
    txt = run_cmd(["tasklist", "/fo", "csv", "/nh"])
    try:
        reader = csv.reader(io.StringIO(txt))
        for row in reader:
            if len(row) >= 2:
                name = row[0].strip()
                pid = row[1].strip()
                if pid.isdigit():
                    pmap[pid] = name
    except Exception:
        pass
    return pmap


def is_private(ip):
    return (
        ip.startswith("10.")
        or ip.startswith("192.168.")
        or ip.startswith("127.")
        or ip.startswith("169.254.")
        or re.match(r"172\.(1[6-9]|2\d|3[01])\.", ip)
        or ip in ("0.0.0.0", "::", "*")
    )


def connections_loop():
    """Echantillonne les connexions actives et accumule le temps par hote distant."""
    while True:
        try:
            txt = run_cmd(["netstat", "-no"] if IS_WINDOWS else ["netstat", "-tun"])
            pmap = get_process_map()
            t = now_ts()
            present = set()
            for line in txt.splitlines():
                parts = line.split()
                if len(parts) < 4:
                    continue
                proto = parts[0].upper()
                if proto not in ("TCP", "UDP"):
                    continue
                # Windows: Proto Local Foreign State PID
                foreign = parts[2]
                state = parts[3] if len(parts) > 3 else ""
                pid = parts[-1] if parts[-1].isdigit() else ""
                if proto == "TCP" and "ESTABLISHED" not in state.upper():
                    continue
                m = re.match(r"\[?([0-9a-fA-F:.]+)\]?:(\d+)$", foreign)
                if not m:
                    continue
                rip, rport = m.group(1), m.group(2)
                if is_private(rip) or ":" in rip and rip.count(":") > 1 and rip in ("::", "::1"):
                    continue
                if is_private(rip):
                    continue
                present.add(rip)
                proc = pmap.get(pid, "") if pid else ""
                with STATE_LOCK:
                    h = CONN_HOSTS.get(rip)
                    if not h:
                        h = {
                            "ip": rip,
                            "host": "",
                            "ports": set(),
                            "procs": set(),
                            "first_seen": t,
                            "last_seen": t,
                            "seconds": 0.0,
                            "last_sample": t,
                        }
                        CONN_HOSTS[rip] = h
                    h["ports"].add(rport)
                    if proc:
                        h["procs"].add(proc)
                    # accumuler le temps depuis le dernier echantillon ou la connexion etait active
                    if t - h["last_sample"] <= 10:
                        h["seconds"] += t - h["last_sample"]
                    h["last_sample"] = t
                    h["last_seen"] = t

            # resolution de noms pour les nouveaux hotes (DNS inverse)
            with STATE_LOCK:
                to_resolve = [ip for ip, h in CONN_HOSTS.items() if not h["host"]][:5]
            for rip in to_resolve:
                hn = resolve_hostname(rip)
                with STATE_LOCK:
                    if rip in CONN_HOSTS:
                        CONN_HOSTS[rip]["host"] = hn or rip

            # enrichissement : organisation proprietaire pour les IP sans DNS inverse
            # (permet d'afficher un nom de service lisible dans Flux & destinations)
            with STATE_LOCK:
                need_org = [
                    ip for ip, h in CONN_HOSTS.items()
                    if h["host"] == ip and ip not in GEO_CACHE
                ][:3]
            for rip in need_org:
                geo_lookup(rip)   # met en cache country/city/org
        except Exception:
            pass
        time.sleep(3)


def geo_loop():
    while True:
        try:
            req = urllib.request.Request(
                "http://ip-api.com/json/?fields=status,country,regionName,city,lat,lon,isp,query",
                headers={"User-Agent": "LAN-Monitor/1.0"},
            )
            with urllib.request.urlopen(req, timeout=8) as resp:
                data = json.loads(resp.read().decode("utf-8"))
            if data.get("status") == "success":
                with STATE_LOCK:
                    GEO.clear()
                    GEO.update(data)
                    GEO["loaded"] = True
        except Exception:
            pass
        time.sleep(600)  # rafraichir toutes les 10 min


# --------------------------------------------------------------------------- #
# Test de debit (speedtest) - a la demande
# --------------------------------------------------------------------------- #
import ssl

SPEED_HOST = "speed.cloudflare.com"


def _ssl_ctx():
    try:
        return ssl.create_default_context()
    except Exception:
        return None


def _open(url, data=None, timeout=30):
    """Ouvre une URL https ; retombe sur un contexte non verifie si le certif echoue."""
    headers = {"User-Agent": "LAN-Monitor/1.0"}
    if data is not None:
        headers["Content-Type"] = "application/octet-stream"
    req = urllib.request.Request(url, data=data, headers=headers)
    try:
        return urllib.request.urlopen(req, timeout=timeout, context=_ssl_ctx())
    except (ssl.SSLError, urllib.error.URLError):
        unverified = ssl.create_default_context()
        unverified.check_hostname = False
        unverified.verify_mode = ssl.CERT_NONE
        return urllib.request.urlopen(req, timeout=timeout, context=unverified)


def tcp_ping(host, port=443, count=5):
    times = []
    for _ in range(count):
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.settimeout(3)
            start = time.perf_counter()
            s.connect((host, port))
            times.append((time.perf_counter() - start) * 1000)
            s.close()
        except Exception:
            pass
        time.sleep(0.08)
    if not times:
        return None, None
    avg = sum(times) / len(times)
    jitter = sum(abs(t - avg) for t in times) / len(times)
    return round(min(times), 1), round(jitter, 1)


def measure_download(nbytes, timeout=30):
    url = f"https://{SPEED_HOST}/__down?bytes={nbytes}"
    total = 0
    start = time.perf_counter()
    with _open(url, timeout=timeout) as resp:
        while True:
            chunk = resp.read(65536)
            if not chunk:
                break
            total += len(chunk)
    dt = time.perf_counter() - start
    return (total * 8 / dt / 1e6) if dt > 0 else 0


def measure_upload(nbytes, timeout=30):
    url = f"https://{SPEED_HOST}/__up"
    payload = b"\x00" * nbytes
    start = time.perf_counter()
    with _open(url, data=payload, timeout=timeout) as resp:
        resp.read()
    dt = time.perf_counter() - start
    return (nbytes * 8 / dt / 1e6) if dt > 0 else 0


def run_speedtest():
    ping, jitter = tcp_ping(SPEED_HOST)
    down = up = 0
    err = ""
    try:
        measure_download(1_000_000, timeout=15)          # warmup
        down = round(measure_download(15_000_000), 2)    # ~15 Mo
    except Exception as e:
        err = f"download: {e}"
    try:
        up = round(measure_upload(6_000_000), 2)         # ~6 Mo
    except Exception as e:
        err = (err + " | " if err else "") + f"upload: {e}"
    return {
        "download_mbps": down,
        "upload_mbps": up,
        "ping_ms": ping,
        "jitter_ms": jitter,
        "server": "Cloudflare (" + SPEED_HOST + ")",
        "ts": datetime.now().strftime("%H:%M:%S"),
        "error": err,
    }


# --------------------------------------------------------------------------- #
# Moteur d'alertes / detection d'attaques externes
# --------------------------------------------------------------------------- #
def geo_lookup(ip):
    """Origine (pays/ville) d'une IP externe, avec cache."""
    if ip in GEO_CACHE:
        return GEO_CACHE[ip]
    info = {"country": "", "city": "", "isp": "", "org": ""}
    try:
        req = urllib.request.Request(
            f"http://ip-api.com/json/{ip}?fields=status,country,city,isp,org",
            headers={"User-Agent": "LAN-Monitor/1.0"},
        )
        with urllib.request.urlopen(req, timeout=6) as resp:
            data = json.loads(resp.read().decode("utf-8"))
        if data.get("status") == "success":
            info = {"country": data.get("country", ""), "city": data.get("city", ""),
                    "isp": data.get("isp", ""), "org": data.get("org", "")}
    except Exception:
        pass
    GEO_CACHE[ip] = info
    return info


def add_alert(severity, category, source, title, detail, defense, origin=None):
    """Ajoute une alerte (dedupliquee sur une fenetre de 2 min)."""
    key = f"{category}|{source}"
    t = now_ts()
    with STATE_LOCK:
        existing = ALERT_INDEX.get(key)
        if existing and (t - existing["ts"]) < 120:
            existing["count"] += 1
            existing["ts"] = t
            existing["ts_h"] = datetime.now().strftime("%H:%M:%S")
            # remonter en tete
            if existing in ALERTS:
                ALERTS.remove(existing)
                ALERTS.insert(0, existing)
            return
        ALERT_SEQ["n"] += 1
        alert = {
            "id": ALERT_SEQ["n"],
            "ts": t,
            "ts_h": datetime.now().strftime("%H:%M:%S"),
            "severity": severity,      # critical | warning | info
            "category": category,
            "source": source,
            "origin": origin or {},
            "title": title,
            "detail": detail,
            "defense": defense,
            "count": 1,
        }
        ALERTS.insert(0, alert)
        ALERT_INDEX[key] = alert
        if len(ALERTS) > 200:
            removed = ALERTS.pop()
            ALERT_INDEX.pop(f"{removed['category']}|{removed['source']}", None)


def split_ipport(s):
    """'1.2.3.4:443' ou '[::1]:80' -> (ip, port)."""
    s = s.strip()
    if s.startswith("["):
        host, _, port = s[1:].partition("]:")
        return host, port
    if s.count(":") > 1:  # IPv6 sans crochets
        return s, ""
    host, _, port = s.rpartition(":")
    return host, port


def parse_netstat_states():
    """Renvoie liste de tuples (proto, laddr, lport, faddr, fport, state)."""
    txt = run_cmd(["netstat", "-no"] if IS_WINDOWS else ["netstat", "-tun"])
    rows = []
    for line in txt.splitlines():
        parts = line.split()
        if len(parts) < 4:
            continue
        proto = parts[0].upper()
        if proto not in ("TCP", "UDP"):
            continue
        laddr, lport = split_ipport(parts[1])
        faddr, fport = split_ipport(parts[2])
        state = parts[3].upper() if len(parts) >= 4 and not parts[3].isdigit() else ""
        rows.append((proto, laddr, lport, faddr, fport, state))
    return rows


DEF_FIREWALL = (
    "Verifiez que le Pare-feu Windows Defender est actif (Parametres > "
    "Confidentialite et securite > Securite Windows > Pare-feu). Bloquez l'IP "
    "source en regle entrante, et lancez une analyse antivirus complete "
    "(Securite Windows > Protection contre les virus > Analyse complete)."
)


def threat_loop():
    """Analyse les connexions pour detecter des attaques entrantes."""
    time.sleep(6)  # laisser la 1re collecte se faire
    while True:
        try:
            rows = parse_netstat_states()
            t = now_ts()

            # 1re passe : recenser les ports reellement en ecoute (services locaux).
            # Un vrai scan/attaque vise CES ports ; les ports locaux ephemeres d'une
            # connexion SORTANTE (navigateur, Claude, streaming...) n'en font pas partie.
            listening = set()
            for proto, laddr, lport, faddr, fport, state in rows:
                if state == "LISTENING" and lport.isdigit():
                    listening.add(int(lport))
                    # Service sensible expose a tout le reseau
                    if laddr in ("0.0.0.0", "::"):
                        p = int(lport)
                        if p in SENSITIVE_PORTS and p not in EXPOSED_SEEN:
                            EXPOSED_SEEN.add(p)
                            add_alert(
                                "warning", "surface", f"port:{p}",
                                f"Service sensible expose : {SENSITIVE_PORTS[p]} (port {p})",
                                f"Le port {p} ({SENSITIVE_PORTS[p]}) ecoute sur toutes les "
                                f"interfaces (0.0.0.0). C'est une porte d'entree potentielle "
                                f"pour une attaque depuis le reseau.",
                                f"Si vous n'utilisez pas {SENSITIVE_PORTS[p]}, desactivez le "
                                f"service. Sinon restreignez-le au pare-feu et n'exposez "
                                f"jamais ce port vers Internet via le routeur.",
                            )

            # 2e passe : ne traiter que le trafic ENTRANT reel.
            for proto, laddr, lport, faddr, fport, state in rows:
                if not faddr or is_private(faddr) or not lport.isdigit():
                    continue
                lp = int(lport)

                # Entrant = poignee de main entrante, OU connexion etablie vers un
                # de NOS ports en ecoute. Tout le reste est du trafic sortant (initie
                # par nous) et n'est jamais une attaque -> ignore.
                is_inbound = (state == "SYN_RECEIVED") or (
                    state == "ESTABLISHED" and lp in listening
                )
                if not is_inbound:
                    continue

                svc = SENSITIVE_PORTS.get(lp, "")

                # Connexion entrante vers un service sensible = critique
                if svc:
                    origin = geo_lookup(faddr)
                    add_alert(
                        "critical", "inbound", faddr,
                        f"Acces entrant a {svc} depuis {faddr}",
                        f"L'hote externe {faddr} atteint votre service {svc} (port {lp}). "
                        f"Vous etes normalement seul sur ce reseau : acces distant "
                        f"potentiellement non autorise.",
                        DEF_FIREWALL, origin,
                    )
                elif state == "SYN_RECEIVED":
                    origin = geo_lookup(faddr)
                    add_alert(
                        "warning", "inbound", faddr,
                        f"Connexion entrante depuis {faddr}",
                        f"Un hote externe ({faddr}) tente d'ouvrir une connexion vers "
                        f"votre machine sur le port {lp}. Inattendu si vous n'hebergez "
                        f"aucun service.",
                        DEF_FIREWALL, origin,
                    )

                # Detection de balayage : meme IP entrante frappant plusieurs de nos ports
                tr = SCAN_TRACK.get(faddr)
                if not tr:
                    tr = {"ports": set(), "first": t, "last": t}
                    SCAN_TRACK[faddr] = tr
                tr["ports"].add(lp)
                tr["last"] = t
                if len(tr["ports"]) >= 6 and (t - tr["first"]) < 120:
                    origin = geo_lookup(faddr)
                    add_alert(
                        "critical", "scan", faddr,
                        f"Balayage de ports depuis {faddr}",
                        f"L'IP {faddr} a sonde {len(tr['ports'])} de vos ports en peu de "
                        f"temps. C'est un scan typique de reconnaissance avant attaque.",
                        "Bloquez immediatement cette IP au pare-feu (regle entrante), "
                        "activez le mode furtif du pare-feu, et verifiez qu'aucun port "
                        "n'est ouvert inutilement. " + DEF_FIREWALL,
                        origin,
                    )

            # nettoyage des vieux suivis de scan
            with STATE_LOCK:
                stale = [ip for ip, tr in SCAN_TRACK.items() if (t - tr["last"]) > 300]
                for ip in stale:
                    SCAN_TRACK.pop(ip, None)
        except Exception:
            pass
        time.sleep(5)


# --------------------------------------------------------------------------- #
# Classification des appareils (type : PC / telephone / routeur ...)
# --------------------------------------------------------------------------- #
def classify_device(dev):
    """Renvoie (type_lisible, icone) a partir du vendor et du nom d'hote."""
    if dev.get("is_gateway"):
        return ("Routeur / Box", "\U0001F310")          # 🌐
    if dev.get("is_self"):
        return ("Cet ordinateur", "\U0001F4BB")         # 💻
    host = (dev.get("hostname") or "").lower()
    vendor = dev.get("vendor", "")
    phone_kw = ("iphone", "android", "galaxy", "redmi", "huawei", "pixel",
                "phone", "oppo", "vivo", "realme", "poco", "xiaomi")
    pc_kw = ("pc", "desktop", "laptop", "macbook", "windows", "dell", "hp",
             "lenovo", "asus", "thinkpad")
    tv_kw = ("tv", "roku", "chromecast", "firetv", "bravia", "shield")
    if any(k in host for k in phone_kw):
        return ("Telephone", "\U0001F4F1")               # 📱
    if any(k in host for k in tv_kw):
        return ("TV / Box multimedia", "\U0001F4FA")     # 📺
    if any(k in host for k in pc_kw):
        return ("Ordinateur", "\U0001F4BB")
    if vendor in ("Samsung", "Xiaomi", "Huawei"):
        return ("Telephone", "\U0001F4F1")
    if vendor == "Apple":
        return ("Appareil Apple", "" if False else "\U0001F34E")  # 🍎
    if vendor == "Raspberry Pi":
        return ("Raspberry Pi", "\U0001F353")            # 🍓
    if vendor in ("TP-Link", "Cisco", "Nokia"):
        return ("Equipement reseau", "\U0001F4F6")       # 📶
    if vendor in ("VMware", "VirtualBox", "Microsoft/Hyper-V"):
        return ("Machine virtuelle", "\U0001F5A5")       # 🖥
    return ("Appareil inconnu", "\U0001F50C")            # 🔌


# --------------------------------------------------------------------------- #
# Resolution du nom de service / site web derriere une IP
# --------------------------------------------------------------------------- #
SERVICE_MAP = {
    "github.com": "GitHub", "githubusercontent.com": "GitHub",
    "githubassets.com": "GitHub",
    "google.com": "Google", "1e100.net": "Google", "gstatic.com": "Google",
    "googleapis.com": "Google", "gvt1.com": "Google", "gvt2.com": "Google",
    "googleusercontent.com": "Google Cloud", "doubleclick.net": "Google Ads",
    "googlevideo.com": "YouTube", "youtube.com": "YouTube", "ytimg.com": "YouTube",
    "anthropic.com": "Anthropic (Claude)", "claude.ai": "Claude",
    "cloudflare.com": "Cloudflare", "cloudflare-dns.com": "Cloudflare",
    "microsoft.com": "Microsoft", "windowsupdate.com": "Windows Update",
    "live.com": "Microsoft", "office.com": "Microsoft 365", "msn.com": "Microsoft",
    "msftconnecttest.com": "Microsoft", "msedge.net": "Microsoft Edge",
    "windows.net": "Microsoft Azure", "office365.com": "Microsoft 365",
    "apple.com": "Apple", "icloud.com": "iCloud", "mzstatic.com": "Apple",
    "facebook.com": "Facebook", "fbcdn.net": "Facebook", "instagram.com": "Instagram",
    "whatsapp.net": "WhatsApp", "whatsapp.com": "WhatsApp",
    "amazonaws.com": "Amazon AWS", "amazon.com": "Amazon",
    "cloudfront.net": "Amazon CloudFront", "media-amazon.com": "Amazon",
    "akamai.net": "Akamai (CDN)", "akamaiedge.net": "Akamai (CDN)",
    "akadns.net": "Akamai", "edgekey.net": "Akamai (CDN)", "edgesuite.net": "Akamai",
    "fastly.net": "Fastly (CDN)", "fbcdn.com": "Facebook",
    "netflix.com": "Netflix", "nflxvideo.net": "Netflix", "nflximg.net": "Netflix",
    "tiktokcdn.com": "TikTok", "tiktokv.com": "TikTok", " tiktok.com": "TikTok",
    "spotify.com": "Spotify", "scdn.co": "Spotify",
    "twitter.com": "Twitter / X", "twimg.com": "Twitter / X", "x.com": "Twitter / X",
    "bing.com": "Bing", "yahoo.com": "Yahoo", "linkedin.com": "LinkedIn",
    "telegram.org": "Telegram", "tdesktop.com": "Telegram", "t.me": "Telegram",
    "discord.com": "Discord", "discord.gg": "Discord", "discordapp.com": "Discord",
    "wikipedia.org": "Wikipedia", "reddit.com": "Reddit", "redd.it": "Reddit",
    "orange.tn": "Orange Tunisie", "ooredoo.tn": "Ooredoo",
}


def registrable_domain(host):
    parts = host.rstrip(".").split(".")
    if len(parts) >= 2:
        return ".".join(parts[-2:]).lower()
    return host.lower()


def service_name(ip, host):
    """Nom lisible du service/site : depuis le DNS inverse, sinon l'organisation geo."""
    if host and host != ip:
        low = host.lower()
        dom = registrable_domain(host)
        if dom in SERVICE_MAP:
            return SERVICE_MAP[dom]
        for key, val in SERVICE_MAP.items():
            if key.strip() and key.strip() in low:
                return val
        sld = dom.split(".")[0]
        if sld and not sld.replace("-", "").isdigit():
            return sld.capitalize()
    # pas de DNS inverse exploitable -> organisation proprietaire de l'IP
    info = GEO_CACHE.get(ip, {})
    org = info.get("org") or info.get("isp")
    if org:
        return org
    return ip


# --------------------------------------------------------------------------- #
# Serialisation pour l'API
# --------------------------------------------------------------------------- #
def devices_payload():
    t = now_ts()
    out = []
    with STATE_LOCK:
        for dev in DEVICES.values():
            dtype, dicon = classify_device(dev)
            out.append({
                "mac": dev["mac"],
                "ip": dev["ip"],
                "vendor": dev.get("vendor", "Inconnu"),
                "type": dtype,
                "icon": dicon,
                "hostname": dev.get("hostname", ""),
                "online": dev.get("online", False),
                "is_gateway": dev.get("is_gateway", False),
                "is_self": dev.get("is_self", False),
                "first_seen": dev.get("first_seen"),
                "last_seen": dev.get("last_seen"),
                "connected_for": (t - dev.get("first_seen", t)) if dev.get("online") else 0,
                "connected_for_h": human_duration(t - dev.get("first_seen", t)) if dev.get("online") else "-",
                "last_seen_h": human_duration(t - dev.get("last_seen", t)) + " ago" if dev.get("last_seen") else "-",
            })
    out.sort(key=lambda d: (not d["online"], not d["is_self"], not d["is_gateway"], d["ip"]))
    return out


def connections_payload():
    out = []
    with STATE_LOCK:
        for h in CONN_HOSTS.values():
            host = h["host"] or h["ip"]
            out.append({
                "ip": h["ip"],
                "host": host,
                "service": service_name(h["ip"], h["host"]),
                "ports": sorted(h["ports"], key=lambda x: int(x))[:6],
                "procs": sorted(h["procs"])[:4],
                "seconds": round(h["seconds"], 1),
                "seconds_h": human_duration(h["seconds"]),
                "last_seen": h["last_seen"],
                "active": (now_ts() - h["last_seen"]) < 8,
            })
    out.sort(key=lambda h: h["seconds"], reverse=True)
    return out[:60]


def alerts_payload():
    with STATE_LOCK:
        items = [dict(a) for a in ALERTS]
    counts = {"critical": 0, "warning": 0, "info": 0}
    for a in items:
        counts[a["severity"]] = counts.get(a["severity"], 0) + 1
    return {"alerts": items, "counts": counts, "total": len(items)}


def overview_payload():
    devs = devices_payload()
    online = [d for d in devs if d["online"]]
    with STATE_LOCK:
        hist = list(TRAFFIC["history"])
        geo = dict(GEO)
    cur_in = hist[-1]["in_bps"] if hist else 0
    cur_out = hist[-1]["out_bps"] if hist else 0
    with STATE_LOCK:
        crit = sum(1 for a in ALERTS if a["severity"] == "critical")
        warn = sum(1 for a in ALERTS if a["severity"] == "warning")
    return {
        "devices_total": len(devs),
        "devices_online": len(online),
        "hosts_tracked": len(CONN_HOSTS),
        "alerts_critical": crit,
        "alerts_warning": warn,
        "cur_in_bps": cur_in,
        "cur_out_bps": cur_out,
        "net": NETINFO,
        "public_ip": geo.get("query", "..."),
        "location": f'{geo.get("city", "")}, {geo.get("country", "")}'.strip(", "),
        "isp": geo.get("isp", ""),
        "server_time": datetime.now().strftime("%H:%M:%S"),
    }


# --------------------------------------------------------------------------- #
# HTTP handler
# --------------------------------------------------------------------------- #
CONTENT_TYPES = {
    ".html": "text/html; charset=utf-8",
    ".css": "text/css; charset=utf-8",
    ".js": "application/javascript; charset=utf-8",
    ".svg": "image/svg+xml",
}


class Handler(BaseHTTPRequestHandler):
    server_version = "LANMonitor/1.0"

    def log_message(self, *a):
        pass  # silencieux

    # -- helpers -------------------------------------------------------- #
    def cookies(self):
        raw = self.headers.get("Cookie", "")
        jar = {}
        for part in raw.split(";"):
            if "=" in part:
                k, v = part.strip().split("=", 1)
                jar[k] = v
        return jar

    def authed(self):
        return session_valid(self.cookies().get("session"))

    def send_json(self, obj, status=200):
        body = json.dumps(obj).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(body)

    def send_file(self, path):
        if not os.path.isfile(path):
            self.send_error(404)
            return
        ext = os.path.splitext(path)[1]
        ctype = CONTENT_TYPES.get(ext, "application/octet-stream")
        with open(path, "rb") as fh:
            body = fh.read()
        self.send_response(200)
        self.send_header("Content-Type", ctype)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def redirect(self, location):
        self.send_response(302)
        self.send_header("Location", location)
        self.end_headers()

    def read_body_json(self):
        length = int(self.headers.get("Content-Length", 0) or 0)
        if not length:
            return {}
        raw = self.rfile.read(length)
        try:
            return json.loads(raw.decode("utf-8"))
        except Exception:
            return {}

    # -- routing -------------------------------------------------------- #
    def do_GET(self):
        path = urlparse(self.path).path

        if path == "/":
            if self.authed():
                self.send_file(os.path.join(WEB_DIR, "index.html"))
            else:
                self.redirect("/login")
            return
        if path == "/login":
            self.send_file(os.path.join(WEB_DIR, "login.html"))
            return

        if path.startswith("/api/"):
            self.handle_api_get(path)
            return

        # fichiers statiques dans /web
        safe = os.path.normpath(path).lstrip("\\/").replace("..", "")
        candidate = os.path.join(WEB_DIR, safe)
        if os.path.commonprefix([os.path.abspath(candidate), WEB_DIR]) == WEB_DIR:
            self.send_file(candidate)
        else:
            self.send_error(404)

    def do_POST(self):
        path = urlparse(self.path).path

        if path == "/api/login":
            data = self.read_body_json()
            if check_credentials(data.get("username", ""), data.get("password", "")):
                token = new_session()
                self.send_response(200)
                self.send_header("Content-Type", "application/json")
                self.send_header(
                    "Set-Cookie",
                    f"session={token}; HttpOnly; Path=/; SameSite=Strict; Max-Age={SESSION_TTL}",
                )
                body = json.dumps({"ok": True}).encode()
                self.send_header("Content-Length", str(len(body)))
                self.end_headers()
                self.wfile.write(body)
            else:
                self.send_json({"ok": False, "error": "Identifiants invalides"}, 401)
            return

        if path == "/api/logout":
            tok = self.cookies().get("session")
            SESSIONS.pop(tok, None)
            self.send_response(200)
            self.send_header("Set-Cookie", "session=; Path=/; Max-Age=0")
            self.send_header("Content-Length", "0")
            self.end_headers()
            return

        if not self.authed():
            self.send_json({"error": "non authentifie"}, 401)
            return

        if path == "/api/change-password":
            data = self.read_body_json()
            current = data.get("current", "")
            new = data.get("new", "")
            if not check_credentials(CONFIG.get("username", "admin"), current):
                self.send_json({"ok": False, "error": "Mot de passe actuel incorrect"}, 400)
                return
            if len(new) < 4:
                self.send_json({"ok": False, "error": "Nouveau mot de passe trop court (min 4)"}, 400)
                return
            CONFIG["salt"] = secrets.token_hex(16)
            CONFIG["password_hash"] = hash_password(new, CONFIG["salt"])
            save_config(CONFIG)
            self.send_json({"ok": True})
            return

        if path == "/api/shutdown":
            self.send_json({"ok": True, "message": "Arret du serveur en cours..."})
            srv = self.server

            def _stop():
                time.sleep(0.6)
                srv.shutdown()

            threading.Thread(target=_stop, daemon=True).start()
            return

        if path == "/api/block":
            # Blocage legitime = via l'API admin du routeur. Pas d'ARP spoofing ici.
            data = self.read_body_json()
            self.send_json({
                "ok": False,
                "manual": True,
                "message": (
                    "Le blocage d'un appareil se fait sur votre routeur (filtrage MAC / "
                    "controle d'acces), pas par interception. Ouvrez l'admin du routeur ("
                    f"http://{NETINFO.get('gateway', '192.168.1.1')}) puis bloquez la MAC "
                    f"{data.get('mac', '')}. Une integration API routeur peut etre ajoutee ici."
                ),
                "gateway": NETINFO.get("gateway", ""),
            })
            return

        self.send_error(404)

    def handle_api_get(self, path):
        if not self.authed():
            self.send_json({"error": "non authentifie"}, 401)
            return
        if path == "/api/overview":
            self.send_json(overview_payload())
        elif path == "/api/devices":
            self.send_json({"devices": devices_payload()})
        elif path == "/api/traffic":
            with STATE_LOCK:
                hist = list(TRAFFIC["history"])
                total_in = TRAFFIC["total_in"]
                total_out = TRAFFIC["total_out"]
            self.send_json({
                "history": hist,
                "total_in": total_in,
                "total_out": total_out,
            })
        elif path == "/api/connections":
            self.send_json({"connections": connections_payload()})
        elif path == "/api/alerts":
            self.send_json(alerts_payload())
        elif path == "/api/speedtest":
            self.send_json(run_speedtest())
        elif path == "/api/geo":
            with STATE_LOCK:
                geo = dict(GEO)
            self.send_json(geo)
        else:
            self.send_error(404)


# --------------------------------------------------------------------------- #
# Demarrage
# --------------------------------------------------------------------------- #
def start_threads():
    load_devices()
    for fn in (scanner_loop, traffic_loop, connections_loop, geo_loop, threat_loop):
        threading.Thread(target=fn, daemon=True).start()


def main():
    start_threads()
    server = ThreadingHTTPServer((HOST, PORT), Handler)
    url = f"http://{HOST}:{PORT}"
    print("=" * 60)
    print("  LAN Monitor - tableau de bord de monitoring local")
    print("=" * 60)
    print(f"  Interface : {url}")
    print(f"  Login     : admin / admin  (a changer dans Parametres)")
    print(f"  Reseau    : {NETINFO['local_ip']}  passerelle {gateway_ip()}")
    print("  Ctrl+C pour arreter.")
    print("=" * 60)
    try:
        server.serve_forever()
        print("\nServeur arrete depuis l'interface. A bientot.")
    except KeyboardInterrupt:
        print("\nArret (Ctrl+C).")
        server.shutdown()


if __name__ == "__main__":
    main()
