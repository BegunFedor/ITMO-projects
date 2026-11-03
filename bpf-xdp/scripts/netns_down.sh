#!/bin/sh
# Убирает тестовую сеть. Удаление одного конца veth удаляет и второй.
# Запускать с sudo.
ip link del xdp-host 2>/dev/null || true
ip netns del xdp-ns 2>/dev/null || true
echo "network is down"
