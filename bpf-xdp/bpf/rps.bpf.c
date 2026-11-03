/*
 * Ограничитель RPS: пропускает не более RPS_LIMIT пакетов в секунду с каждого
 * IPv4-адреса, остальные отбрасывает. Считает статистику по каждому адресу.
 *
 * Это «голый» вариант без libbpf: карты (maps) описаны в секции "maps" в
 * старом формате, а вспомогательные функции ядра (helpers) объявлены вручную.
 * Загрузчик load_xdp сам создаёт карты и подставляет их в код (см. load_xdp.c).
 */
#include <linux/bpf.h>

#include "common.h"
#include "net.h"

#define NULL ((void*)0)
#define SEC(name) __attribute__((section(name), used))

/* Описание карты. Загрузчик читает такие структуры из секции "maps". */
struct bpf_map_def {
  __u32 type;
  __u32 key_size;
  __u32 value_size;
  __u32 max_entries;
  __u32 map_flags;
};

/*
 * Вспомогательные функции ядра вызываются по номеру. Тип «указатель на
 * функцию с номером N» - стандартный приём: компилятор превращает вызов в
 * инструкцию `call N`, а ядро по номеру подставляет настоящую функцию.
 */
static void* (*bpf_map_lookup_elem)(void* map, const void* key) = (void*)
    BPF_FUNC_map_lookup_elem;
static long (*bpf_map_update_elem)(
    void* map, const void* key, const void* value, __u64 flags
) = (void*)BPF_FUNC_map_update_elem;
static __u64 (*bpf_ktime_get_ns)(void) = (void*)BPF_FUNC_ktime_get_ns;

/*
 * rates: «когда началось окно и сколько пакетов в нём». LRU-таблица сама
 * выбрасывает давно не виденные адреса, поэтому её нельзя переполнить, рассылая
 * пакеты с множества разных адресов.
 */
SEC("maps")
struct bpf_map_def rates = {
    .type = BPF_MAP_TYPE_LRU_HASH,
    .key_size = sizeof(__u32),
    .value_size = sizeof(struct rate_entry),
    .max_entries = 1024,
};

/* stats: сколько пропущено и сколько отброшено, по каждому адресу. */
SEC("maps")
struct bpf_map_def stats = {
    .type = BPF_MAP_TYPE_HASH,
    .key_size = sizeof(__u32),
    .value_size = sizeof(struct stat_entry),
    .max_entries = 1024,
};

SEC("license") char _license[] = "GPL";

/* Возвращает 1, если пакет с адреса `src` нужно отбросить. */
static __always_inline int over_limit(__u32 src) {
  __u64 now = bpf_ktime_get_ns();

  struct rate_entry* rate = bpf_map_lookup_elem(&rates, &src);
  if (rate == NULL) {
    /* Первый пакет с этого адреса: заводим запись. */
    struct rate_entry fresh = {.window_start_ns = now, .count = 0};
    bpf_map_update_elem(&rates, &src, &fresh, BPF_NOEXIST);
    rate = bpf_map_lookup_elem(&rates, &src);
    if (rate == NULL) {
      return 0; /* не получилось - лучше пропустить, чем потерять пакет */
    }
  }

  /* Прошла секунда - начинаем новое окно. */
  if (now - rate->window_start_ns >= RPS_WINDOW_NS) {
    rate->window_start_ns = now;
    rate->count = 0;
  }

  /* Атомарное увеличение: пакеты могут обрабатываться на разных ядрах CPU. */
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

SEC("xdp") enum xdp_action rps(struct xdp_md* ctx) {
  void* data = (void*)(long)ctx->data;
  void* data_end = (void*)(long)ctx->data_end;

  struct eth* eth = data;
  if ((void*)(eth + 1) > data_end) {
    return XDP_PASS;
  }
  if (eth->type != NET16(ETH_P_IPV4)) {
    return XDP_PASS; /* ARP и всё прочее не ограничиваем */
  }

  struct ipv4* ip = (struct ipv4*)(eth + 1);
  if ((void*)(ip + 1) > data_end) {
    return XDP_PASS;
  }

  int drop = over_limit(ip->src);
  count_packet(ip->src, drop);
  return drop ? XDP_DROP : XDP_PASS;
}
