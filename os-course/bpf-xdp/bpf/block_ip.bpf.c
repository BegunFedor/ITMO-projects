/* Не пропускает пакеты, отправленные с адреса 10.200.1.2. */
#include <linux/bpf.h>

#include "net.h"

#define BLOCKED_IP IPV4(10, 200, 1, 2)

__attribute__((section("xdp"), used)) enum xdp_action block_ip(
    struct xdp_md* ctx
) {
  void* data = (void*)(long)ctx->data;
  void* data_end = (void*)(long)ctx->data_end;

  struct eth* eth = data;
  if ((void*)(eth + 1) > data_end) {
    return XDP_PASS;
  }
  /* Нас интересует только IPv4; ARP и прочее пропускаем. */
  if (eth->type != NET16(ETH_P_IPV4)) {
    return XDP_PASS;
  }

  struct ipv4* ip = (struct ipv4*)(eth + 1);
  if ((void*)(ip + 1) > data_end) {
    return XDP_PASS;
  }

  if (ip->src == BLOCKED_IP) {
    return XDP_DROP;
  }
  return XDP_PASS;
}
