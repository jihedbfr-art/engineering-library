#!/usr/bin/env python3
"""Scanner de ports TCP educatif, limite a un seul hote et une plage de ports raisonnable.

AVERTISSEMENT: a utiliser UNIQUEMENT sur des machines dont vous etes proprietaire ou que vous
etes explicitement autorise a tester. Scanner des ports sur des systemes tiers sans autorisation
peut etre illegal selon la juridiction. Ce script est concu a des fins pedagogiques : cible par
defaut = 127.0.0.1 (localhost), pas de mode scan de plages IP entieres, pas de mode agressif/masse.
"""
import argparse
import socket
import sys

WARNING = (
    "AVERTISSEMENT: ce scanner de ports est a usage EDUCATIF uniquement.\n"
    "A utiliser UNIQUEMENT sur des machines dont vous etes proprietaire ou que vous etes\n"
    "explicitement autorise a tester. Le scan de systemes tiers sans autorisation peut etre illegal.\n"
)

# Cibles autorisees : uniquement la machine locale. Volontairement restrictif pour eviter
# tout usage detourne (pas de scan de plages IP, pas de cible arbitraire distante).
ALLOWED_HOSTS = {"127.0.0.1", "localhost", "::1"}

MAX_PORT_RANGE = 1024  # pas de scan de masse : plage limitee a 1024 ports max


def scan_port(host, port, timeout=0.5):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
        sock.settimeout(timeout)
        result = sock.connect_ex((host, port))
        return result == 0


def main():
    parser = argparse.ArgumentParser(
        description="Scanner de ports TCP educatif (localhost uniquement).",
        epilog=WARNING,
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("--host", default="127.0.0.1",
                         help="Hote a scanner (defaut et recommande: 127.0.0.1). "
                              "Limite aux hotes locaux : 127.0.0.1, localhost, ::1")
    parser.add_argument("--start-port", type=int, default=1, help="Premier port de la plage (defaut: 1)")
    parser.add_argument("--end-port", type=int, default=1024, help="Dernier port de la plage (defaut: 1024)")
    parser.add_argument("--timeout", type=float, default=0.5, help="Timeout par port en secondes (defaut: 0.5)")
    args = parser.parse_args()

    print(WARNING)

    if args.host not in ALLOWED_HOSTS:
        print(
            f"Erreur: hote '{args.host}' non autorise. Ce scanner educatif se limite a la "
            f"machine locale ({', '.join(sorted(ALLOWED_HOSTS))}) pour eviter tout usage detourne.",
            file=sys.stderr,
        )
        sys.exit(1)

    if args.start_port < 1 or args.end_port > 65535 or args.start_port > args.end_port:
        print("Erreur: plage de ports invalide.", file=sys.stderr)
        sys.exit(1)

    port_count = args.end_port - args.start_port + 1
    if port_count > MAX_PORT_RANGE:
        print(
            f"Erreur: plage trop large ({port_count} ports). Maximum autorise: {MAX_PORT_RANGE} ports "
            f"(pas de scan de masse). Reduisez --start-port/--end-port.",
            file=sys.stderr,
        )
        sys.exit(1)

    print(f"Scan de {args.host}, ports {args.start_port}-{args.end_port} (timeout {args.timeout}s/port)...\n")

    open_ports = []
    for port in range(args.start_port, args.end_port + 1):
        if scan_port(args.host, port, args.timeout):
            open_ports.append(port)
            print(f"  Port {port}: OUVERT")

    print(f"\nScan termine. {len(open_ports)} port(s) ouvert(s) sur {port_count} scanne(s).")
    if open_ports:
        print(f"Ports ouverts: {', '.join(str(p) for p in open_ports)}")


if __name__ == "__main__":
    main()
