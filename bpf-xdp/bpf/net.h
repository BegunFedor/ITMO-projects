/*
 * Заголовки сетевых протоколов, которые нужны фильтрам.
 *
 * Пакет приходит в XDP-программу «как есть»: сначала ethernet-заголовок,
 * сразу за ним IPv4-заголовок. Мы описываем их структурами, чтобы читать поля
 * по именам, а не по номерам байтов.
 */
#pragma once

#include <linux/types.h>

/* __attribute__((packed)) запрещает компилятору вставлять пустые байты
 * (выравнивание) между полями: расположение должно совпадать с сетевым. */
struct eth {
  __u8 dst[6];
  __u8 src[6];
  __u16 type;
} __attribute__((packed));

struct ipv4 {
  __u8 version_ihl;
  __u8 tos;
  __u16 length;
  __u16 id;
  __u16 fragment;
  __u8 ttl;
  __u8 protocol;
  __u16 checksum;
  __u32 src;
  __u32 dst;
} __attribute__((packed));

/*
 * В сети числа передаются «старшим байтом вперёд» (big-endian), а наши
 * процессоры (x86 и ARM) хранят «младшим вперёд» (little-endian). Поэтому
 * константы нужно переворачивать, прежде чем сравнивать с полями пакета.
 */
#if __BYTE_ORDER__ == __ORDER_LITTLE_ENDIAN__
#define NET16(x) __builtin_bswap16(x)
#define NET32(x) __builtin_bswap32(x)
#else
#define NET16(x) (x)
#define NET32(x) (x)
#endif

#define ETH_P_IPV4 0x0800
#define IPV4(a, b, c, d) \
  NET32(((__u32)(a) << 24) | ((b) << 16) | ((c) << 8) | (d))
