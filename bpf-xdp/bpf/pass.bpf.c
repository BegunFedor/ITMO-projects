/* Самый простой фильтр: пропускает всё. */
#include <linux/bpf.h>

__attribute__((section("xdp"), used)) enum xdp_action pass(struct xdp_md* ctx) {
  (void)ctx;
  return XDP_PASS;
}
