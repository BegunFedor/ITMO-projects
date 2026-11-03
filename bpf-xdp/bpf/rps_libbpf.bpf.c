/*
 * Тот же ограничитель RPS, но с libbpf. Сравните с rps.bpf.c:
 *  - карты описываются типизированными структурами в секции ".maps" (с BTF),
 *    и загрузчику не нужно разбирать их вручную;
 *  - вспомогательные функции ядра объявлены в <bpf/bpf_helpers.h>;
 *  - релокации (подстановку fd карт) делает сама libbpf.
 */
/* Порядок важен: сначала типы ядра, потом помощники libbpf. */
/* clang-format off */
#include <linux/bpf.h>
#include <bpf/bpf_helpers.h>

#include "common.h"
#include "net.h"
/* clang-format on */

#ifndef NULL
#define NULL ((void*)0)
#endif

struct {
  __uint(type, BPF_MAP_TYPE_LRU_HASH);
  __uint(max_entries, 1024);
  __type(key, __u32);
  __type(value, struct rate_entry);
} rates SEC(".maps");

struct {
  __uint(type, BPF_MAP_TYPE_HASH);
  __uint(max_entries, 1024);
  __type(key, __u32);
  __type(value, struct stat_entry);
} stats SEC(".maps");

char _license[] SEC("license") = "GPL";

static __always_inline int over_limit(__u32 src) {
  __u64 now = bpf_ktime_get_ns();

  struct rate_entry* rate = bpf_map_lookup_elem(&rates, &src);
  if (rate == NULL) {
    struct rate_entry fresh = {.window_start_ns = now, .count = 0};
    bpf_map_update_elem(&rates, &src, &fresh, BPF_NOEXIST);
    rate = bpf_map_lookup_elem(&rates, &src);
    if (rate == NULL) {
      return 0;
    }
  }

  if (now - rate->window_start_ns >= RPS_WINDOW_NS) {
    rate->window_start_ns = now;
    rate->count = 0;
  }

  __u32 seen = __sync_add_and_fetch(&rate->count, 1);
  return seen > RPS_LIMIT;
}

static __always_inline void count_packet(__u32 src, int dropped) {
  struct stat_entry* st = bpf_map_lookup_elem(&stats, &src);
  if (st == NULL) {
    struct stat_entry zero = {};
    bpf_map_update_elem(&stats, &src, &zero, BPF_NOEXIST);
    st = bpf_map_lookup_elem(&stats, &src);
    if (st == NULL) {
      return;
    }
  }
  if (dropped) {
    __sync_fetch_and_add(&st->dropped, 1);
  } else {
    __sync_fetch_and_add(&st->passed, 1);
  }
}

SEC("xdp")
int rps(struct xdp_md* ctx) {
  void* data = (void*)(long)ctx->data;
  void* data_end = (void*)(long)ctx->data_end;

  struct eth* eth = data;
  if ((void*)(eth + 1) > data_end) {
    return XDP_PASS;
  }
  if (eth->type != NET16(ETH_P_IPV4)) {
    return XDP_PASS;
  }

  struct ipv4* ip = (struct ipv4*)(eth + 1);
  if ((void*)(ip + 1) > data_end) {
    return XDP_PASS;
  }

  int drop = over_limit(ip->src);
  count_packet(ip->src, drop);
  return drop ? XDP_DROP : XDP_PASS;
}
