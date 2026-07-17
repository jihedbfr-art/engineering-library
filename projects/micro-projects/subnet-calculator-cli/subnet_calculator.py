import argparse
import ipaddress


def main():
    parser = argparse.ArgumentParser(description="Calculateur de sous-reseau IPv4 (CIDR)")
    parser.add_argument("cidr", help="ex: 192.168.1.0/24")
    args = parser.parse_args()

    net = ipaddress.ip_network(args.cidr, strict=False)
    print(f"Adresse reseau   : {net.network_address}")
    print(f"Masque           : {net.netmask}")
    print(f"Broadcast        : {net.broadcast_address}")
    print(f"Nb d'hotes utiles: {max(net.num_addresses - 2, 0)}")
    hosts = list(net.hosts())
    if hosts:
        print(f"Premiere adresse : {hosts[0]}")
        print(f"Derniere adresse : {hosts[-1]}")


if __name__ == "__main__":
    main()
