#!/bin/sh
# Поднимает тестовую сеть: namespace xdp-ns и соединённые veth xdp-host <-> xdp-peer.
# Запускать с sudo.
set -e

ip netns add xdp-ns
ip link add xdp-host type veth peer name xdp-peer
ip link set xdp-peer netns xdp-ns

ip           addr add 10.200.1.1/24 dev xdp-host
ip -n xdp-ns addr add 10.200.1.2/24 dev xdp-peer
ip -n xdp-ns addr add 10.200.1.3/24 dev xdp-peer

ip           link set xdp-host up
ip -n xdp-ns link set xdp-peer up
ip -n xdp-ns link set lo       up
echo "network is up: host 10.200.1.1, namespace xdp-ns has 10.200.1.2 and 10.200.1.3"
