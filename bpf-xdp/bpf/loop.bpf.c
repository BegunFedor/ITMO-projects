/*
 * Фильтр, работа которого не завершается. verifier обязан отвергнуть его:
 * программа, которая может зависнуть, остановила бы обработку пакетов (и
 * всё ядро вместе с ней).
 */
#include <linux/bpf.h>

__attribute__((section("xdp"), used)) enum xdp_action loop(struct xdp_md* ctx) {
  (void)ctx;
  for (;;) {
    __asm__ volatile("" ::: "memory");
  }
  return XDP_PASS;
}
