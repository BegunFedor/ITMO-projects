#!/bin/sh
# Показательный прогон: фильтр блокировки IP и ограничитель RPS.
# Запускать из корня лабораторной с sudo:  sudo scripts/demo.sh
set -e
cd "$(dirname "$0")/.."

make -s
scripts/netns_down.sh >/dev/null
scripts/netns_up.sh

echo
echo "=== 1. без фильтра: оба адреса пингуются ==="
ip netns exec xdp-ns ping -I 10.200.1.2 -c 3 -W 1 10.200.1.1 | tail -2
ip netns exec xdp-ns ping -I 10.200.1.3 -c 3 -W 1 10.200.1.1 | tail -2

echo
echo "=== 2. фильтр block_ip: 10.200.1.2 заблокирован ==="
./load_xdp bpf/block_ip.bpf.o xdp-host --seconds 8 &
sleep 1
ip netns exec xdp-ns ping -I 10.200.1.2 -c 3 -W 1 10.200.1.1 | tail -2 || true
ip netns exec xdp-ns ping -I 10.200.1.3 -c 3 -W 1 10.200.1.1 | tail -2
wait

echo
echo "=== 3. ограничитель RPS (5 пакетов в секунду на адрес), 6 секунд ==="
./load_xdp bpf/rps.bpf.o xdp-host --seconds 8 &
sleep 1
timeout 6 ip netns exec xdp-ns ping -I 10.200.1.2 -f -c 10000 10.200.1.1 >/dev/null 2>&1 || true
ip netns exec xdp-ns ping -I 10.200.1.3 -c 3 -W 1 10.200.1.1 | tail -2
wait

scripts/netns_down.sh
