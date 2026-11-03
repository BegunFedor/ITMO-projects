/*
 * Этот фильтр verifier НЕ ПРОПУСТИТ (Permission denied): мы читаем поле
 * ethernet-заголовка, не убедившись, что пакет вообще настолько длинный.
 * Он нужен, чтобы посмотреть на диагностику verifier.
 */
#include <linux/bpf.h>

#include "net.h"

__attribute__((section("xdp"), used)) enum xdp_action bad(struct xdp_md* ctx) {
  void* data = (void*)(long)ctx->data;
  struct eth* eth = data;

  if (eth->type == NET16(ETH_P_IPV4)) {
    return XDP_DROP;
  }
  return XDP_PASS;
}
