/* Исправленный вариант bad.bpf.c: блокирует любые IPv4-кадры. */
#include <linux/bpf.h>

#include "net.h"

__attribute__((section("xdp"), used)) enum xdp_action drop_ipv4(
    struct xdp_md* ctx
) {
  void* data = (void*)(long)ctx->data;
  void* data_end = (void*)(long)ctx->data_end;
  struct eth* eth = data;

  /* Доказываем verifier'у: весь ethernet-заголовок лежит внутри пакета. */
  if ((void*)(eth + 1) > data_end) {
    return XDP_PASS;
  }

  if (eth->type == NET16(ETH_P_IPV4)) {
    return XDP_DROP;
  }
  return XDP_PASS;
}
