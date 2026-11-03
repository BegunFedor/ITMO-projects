/*
 * load_xdp - загрузчик XDP-фильтров на «голых» системных вызовах bpf(2).
 *
 * Без libbpf и без libelf: мы сами читаем ELF-файл, создаём карты (maps),
 * исправляем в байткоде ссылки на них (релокации), загружаем программу в ядро,
 * тестируем её и подключаем к сетевому интерфейсу.
 *
 * Использование:
 *   load_xdp --test <prog.o|prog.bin> [--src A.B.C.D] [--count N] [--proto HEX]
 *   load_xdp <prog.o|prog.bin> <iface> [--skb] [--seconds N]
 * Общий флаг: -v (показать лог verifier даже при успехе).
 *
 * Файл .o - полный объектный файл (нужен, если в программе есть maps).
 * Файл .bin - только байткод (то, что выдаёт llvm-objcopy --dump-section).
 */
#define _GNU_SOURCE

#include <arpa/inet.h>
#include <elf.h>
#include <errno.h>
#include <fcntl.h>
#include <linux/bpf.h>
#include <net/if.h>
#include <signal.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/syscall.h>
#include <time.h>
#include <unistd.h>

#include "bpf/common.h"

/* Флаг «generic»-режима XDP (из linux/if_link.h): работает на любом интерфейсе.
 */
#define XDP_FLAGS_SKB_MODE (1U << 1)

#define LOG_SIZE (1 << 20)
#define MAX_MAPS 16
#define PROG_SECTION "xdp"
#define MAPS_SECTION "maps"

/* Формат описания карты в секции "maps" (тот же, что в rps.bpf.c). */
struct bpf_map_def {
  uint32_t type;
  uint32_t key_size;
  uint32_t value_size;
  uint32_t max_entries;
  uint32_t map_flags;
};

struct map_info {
  char name[BPF_OBJ_NAME_LEN];
  int fd;
  uint32_t sym_index; /* номер символа в таблице символов ELF */
  uint32_t key_size;
  uint32_t value_size;
};

struct program {
  struct bpf_insn* insns; /* байткод */
  size_t insn_cnt;        /* число инструкций */
  char license[64];
  struct map_info maps[MAX_MAPS];
  size_t map_cnt;
  int fd; /* дескриптор загруженной программы */
};

static int verbose = 0;
static volatile sig_atomic_t stop = 0;

/* ------------------------------------------------------------------------- */
/* Системный вызов bpf                                                       */
/* ------------------------------------------------------------------------- */

static int sys_bpf(enum bpf_cmd cmd, union bpf_attr* attr, unsigned int size) {
  return (int)syscall(SYS_bpf, cmd, attr, size);
}

static void die(const char* msg) {
  fprintf(stderr, "load_xdp: %s\n", msg);
  exit(1);
}

static void die_errno(const char* what) {
  fprintf(stderr, "load_xdp: %s: %s\n", what, strerror(errno));
  exit(1);
}

/* ------------------------------------------------------------------------- */
/* Чтение файла и разбор ELF                                                 */
/* ------------------------------------------------------------------------- */

static unsigned char* read_file(const char* path, size_t* size) {
  FILE* f = fopen(path, "rb");
  if (f == NULL) {
    die_errno(path);
  }
  fseek(f, 0, SEEK_END);
  long n = ftell(f);
  fseek(f, 0, SEEK_SET);
  if (n <= 0) {
    die("empty file");
  }
  unsigned char* buf = malloc((size_t)n);
  if (buf == NULL || fread(buf, 1, (size_t)n, f) != (size_t)n) {
    die_errno("read");
  }
  fclose(f);
  *size = (size_t)n;
  return buf;
}

struct elf_file {
  unsigned char* data;
  size_t size;
  Elf64_Ehdr* eh;
  Elf64_Shdr* sh;
  const char* shstr;
};

static const void* elf_at(
    const struct elf_file* e, uint64_t off, uint64_t len
) {
  if (off > e->size || len > e->size - off) {
    die("ELF file is truncated or corrupted");
  }
  return e->data + off;
}

static void elf_open(struct elf_file* e, unsigned char* data, size_t size) {
  e->data = data;
  e->size = size;
  e->eh = (Elf64_Ehdr*)elf_at(e, 0, sizeof(Elf64_Ehdr));
  if (memcmp(e->eh->e_ident, ELFMAG, SELFMAG) != 0 ||
      e->eh->e_ident[EI_CLASS] != ELFCLASS64 || e->eh->e_machine != EM_BPF) {
    die("not a 64-bit BPF ELF object");
  }
  e->sh = (Elf64_Shdr*)elf_at(
      e, e->eh->e_shoff, (uint64_t)e->eh->e_shnum * sizeof(Elf64_Shdr)
  );
  const Elf64_Shdr* strs = &e->sh[e->eh->e_shstrndx];
  e->shstr = elf_at(e, strs->sh_offset, strs->sh_size);
}

static int elf_find(const struct elf_file* e, const char* name) {
  for (int i = 0; i < e->eh->e_shnum; i++) {
    if (strcmp(e->shstr + e->sh[i].sh_name, name) == 0) {
      return i;
    }
  }
  return -1;
}

static const void* elf_section(const struct elf_file* e, int idx) {
  return elf_at(e, e->sh[idx].sh_offset, e->sh[idx].sh_size);
}

/* ------------------------------------------------------------------------- */
/* Создание карт и релокации                                                 */
/* ------------------------------------------------------------------------- */

static int create_map(const char* name, const struct bpf_map_def* def) {
  union bpf_attr attr;
  memset(&attr, 0, sizeof attr);
  attr.map_type = def->type;
  attr.key_size = def->key_size;
  attr.value_size = def->value_size;
  attr.max_entries = def->max_entries;
  attr.map_flags = def->map_flags;
  strncpy(attr.map_name, name, BPF_OBJ_NAME_LEN - 1);
  int fd = sys_bpf(BPF_MAP_CREATE, &attr, sizeof attr);
  if (fd < 0) {
    die_errno("BPF_MAP_CREATE");
  }
  return fd;
}

/* Находит в секции "maps" все описания и создаёт по ним карты. */
static void create_maps(
    const struct elf_file* e, int maps_idx, struct program* prog
) {
  const unsigned char* maps = elf_section(e, maps_idx);
  int symtab_idx = -1;
  for (int i = 0; i < e->eh->e_shnum; i++) {
    if (e->sh[i].sh_type == SHT_SYMTAB) {
      symtab_idx = i;
    }
  }
  if (symtab_idx < 0) {
    die("no symbol table");
  }
  const Elf64_Sym* syms = elf_section(e, symtab_idx);
  size_t nsyms = e->sh[symtab_idx].sh_size / sizeof(Elf64_Sym);
  const Elf64_Shdr* strtab = &e->sh[e->sh[symtab_idx].sh_link];
  const char* names = elf_at(e, strtab->sh_offset, strtab->sh_size);

  for (size_t i = 0; i < nsyms; i++) {
    if (syms[i].st_shndx != maps_idx ||
        ELF64_ST_TYPE(syms[i].st_info) != STT_OBJECT) {
      continue;
    }
    if (prog->map_cnt == MAX_MAPS) {
      die("too many maps");
    }
    if (syms[i].st_value + sizeof(struct bpf_map_def) >
        e->sh[maps_idx].sh_size) {
      die("map definition is out of the maps section");
    }
    const struct bpf_map_def* def =
        (const struct bpf_map_def*)(maps + syms[i].st_value);
    struct map_info* m = &prog->maps[prog->map_cnt++];
    snprintf(m->name, sizeof m->name, "%s", names + syms[i].st_name);
    m->sym_index = (uint32_t)i;
    m->key_size = def->key_size;
    m->value_size = def->value_size;
    m->fd = create_map(m->name, def);
    if (verbose) {
      fprintf(stderr, "created map '%s' (fd %d)\n", m->name, m->fd);
    }
  }
}

/*
 * Релокация: компилятор не знает, под каким номером (fd) ядро выдаст нашу
 * карту, поэтому в инструкции `ldimm64` (загрузка 64-битной константы) он
 * оставляет пустое место, а в таблице релокаций записывает «здесь должен быть
 * адрес символа X». Мы подставляем туда fd и помечаем инструкцию
 * src_reg = BPF_PSEUDO_MAP_FD: «в imm лежит не число, а fd карты».
 */
static void apply_relocations(
    const struct elf_file* e, int prog_idx, int maps_idx, struct program* prog
) {
  int symtab_idx = -1;
  for (int i = 0; i < e->eh->e_shnum; i++) {
    if (e->sh[i].sh_type == SHT_SYMTAB) {
      symtab_idx = i;
    }
  }
  const Elf64_Sym* syms = elf_section(e, symtab_idx);

  for (int s = 0; s < e->eh->e_shnum; s++) {
    if (e->sh[s].sh_type != SHT_REL || (int)e->sh[s].sh_info != prog_idx) {
      continue;
    }
    const Elf64_Rel* rels = elf_section(e, s);
    size_t nrels = e->sh[s].sh_size / sizeof(Elf64_Rel);
    for (size_t r = 0; r < nrels; r++) {
      uint32_t sym = ELF64_R_SYM(rels[r].r_info);
      size_t idx = rels[r].r_offset / sizeof(struct bpf_insn);
      if (idx >= prog->insn_cnt) {
        die("relocation points outside of the program");
      }
      struct bpf_insn* insn = &prog->insns[idx];
      if (insn->code != (BPF_LD | BPF_IMM | BPF_DW)) {
        die("relocation does not point to a ldimm64 instruction");
      }
      if (syms[sym].st_shndx != maps_idx) {
        die("unsupported relocation: only references to maps are handled");
      }
      struct map_info* found = NULL;
      for (size_t k = 0; k < prog->map_cnt; k++) {
        if (prog->maps[k].sym_index == sym) {
          found = &prog->maps[k];
        }
      }
      if (found == NULL) {
        die("relocation refers to an unknown map");
      }
      insn->src_reg = BPF_PSEUDO_MAP_FD;
      insn->imm = found->fd;
    }
  }
}

/* ------------------------------------------------------------------------- */
/* Загрузка программы в ядро                                                 */
/* ------------------------------------------------------------------------- */

static void load_program(struct program* prog, const char* name) {
  static char log[LOG_SIZE];
  union bpf_attr attr;
  memset(&attr, 0, sizeof attr);
  attr.prog_type = BPF_PROG_TYPE_XDP;
  attr.insn_cnt = (uint32_t)prog->insn_cnt;
  attr.insns = (uint64_t)(uintptr_t)prog->insns;
  attr.license = (uint64_t)(uintptr_t)prog->license;
  /* Лог verifier: без него при отказе мы увидим лишь «Permission denied». */
  attr.log_level = 1;
  attr.log_buf = (uint64_t)(uintptr_t)log;
  attr.log_size = sizeof log;
  strncpy(attr.prog_name, name, BPF_OBJ_NAME_LEN - 1);

  prog->fd = sys_bpf(BPF_PROG_LOAD, &attr, sizeof attr);
  if (prog->fd < 0) {
    int err = errno;
    fprintf(stderr, "load_xdp: BPF_PROG_LOAD failed: %s\n", strerror(err));
    fprintf(stderr, "---- verifier log ----\n%s----------------------\n", log);
    exit(1);
  }
  if (verbose) {
    fprintf(stderr, "---- verifier log ----\n%s----------------------\n", log);
  }
}

static void program_from_file(struct program* prog, const char* path) {
  memset(prog, 0, sizeof *prog);
  snprintf(prog->license, sizeof prog->license, "GPL");

  size_t size = 0;
  unsigned char* data = read_file(path, &size);

  if (size >= SELFMAG && memcmp(data, ELFMAG, SELFMAG) == 0) {
    struct elf_file e;
    elf_open(&e, data, size);

    int prog_idx = elf_find(&e, PROG_SECTION);
    if (prog_idx < 0) {
      die("no 'xdp' section in the object file");
    }
    size_t bytes = e.sh[prog_idx].sh_size;
    if (bytes == 0 || bytes % sizeof(struct bpf_insn) != 0) {
      die("bad size of the program section");
    }
    prog->insns = malloc(bytes);
    memcpy(prog->insns, elf_section(&e, prog_idx), bytes);
    prog->insn_cnt = bytes / sizeof(struct bpf_insn);

    int lic_idx = elf_find(&e, "license");
    if (lic_idx >= 0) {
      size_t n = e.sh[lic_idx].sh_size;
      snprintf(
          prog->license,
          sizeof prog->license,
          "%.*s",
          (int)(n < sizeof prog->license - 1 ? n : sizeof prog->license - 1),
          (const char*)elf_section(&e, lic_idx)
      );
    }

    int maps_idx = elf_find(&e, MAPS_SECTION);
    if (maps_idx >= 0) {
      create_maps(&e, maps_idx, prog);
    }
    apply_relocations(&e, prog_idx, maps_idx, prog);
    free(data); /* байткод уже скопирован, сам файл больше не нужен */
  } else {
    /* Сырой байткод без заголовков: релокаций и карт здесь быть не может. */
    if (size % sizeof(struct bpf_insn) != 0) {
      die("raw program size is not a multiple of 8 bytes");
    }
    prog->insns = (struct bpf_insn*)data;
    prog->insn_cnt = size / sizeof(struct bpf_insn);
  }
}

/* ------------------------------------------------------------------------- */
/* Тестовый запуск: BPF_PROG_TEST_RUN                                        */
/* ------------------------------------------------------------------------- */

static const char* action_name(uint32_t action) {
  switch (action) {
    case XDP_ABORTED:
      return "XDP_ABORTED";
    case XDP_DROP:
      return "XDP_DROP";
    case XDP_PASS:
      return "XDP_PASS";
    case XDP_TX:
      return "XDP_TX";
    case XDP_REDIRECT:
      return "XDP_REDIRECT";
    default:
      return "?";
  }
}

/* Собирает минимальный ethernet+IPv4+UDP-кадр длиной 64 байта. */
static size_t build_packet(uint8_t* pkt, uint32_t src_be, uint16_t proto) {
  memset(pkt, 0, 64);
  memset(pkt, 0xff, 6); /* получатель: broadcast */
  pkt[6] = 0x02; /* отправитель: произвольный MAC */
  pkt[11] = 0x01;
  pkt[12] = (uint8_t)(proto >> 8); /* тип кадра: 0x0800 = IPv4 */
  pkt[13] = (uint8_t)(proto & 0xff);
  pkt[14] = 0x45; /* IPv4, длина заголовка 20 байт */
  pkt[16] = 0;
  pkt[17] = 64 - 14; /* общая длина IP-пакета */
  pkt[22] = 64;      /* TTL */
  pkt[23] = 17;      /* протокол UDP */
  memcpy(pkt + 26, &src_be, 4);
  uint32_t dst_be = inet_addr("10.200.1.1");
  memcpy(pkt + 30, &dst_be, 4);
  return 64;
}

static int test_run(
    int prog_fd, const uint8_t* pkt, size_t len, uint32_t* ret
) {
  uint8_t out[256];
  union bpf_attr attr;
  memset(&attr, 0, sizeof attr);
  attr.test.prog_fd = (uint32_t)prog_fd;
  attr.test.data_in = (uint64_t)(uintptr_t)pkt;
  attr.test.data_size_in = (uint32_t)len;
  attr.test.data_out = (uint64_t)(uintptr_t)out;
  attr.test.data_size_out = sizeof out;
  attr.test.repeat = 1;
  int r = sys_bpf(BPF_PROG_TEST_RUN, &attr, sizeof attr);
  *ret = attr.test.retval;
  return r;
}

/* ------------------------------------------------------------------------- */
/* Чтение статистики из карты stats                                          */
/* ------------------------------------------------------------------------- */

static void print_stats(const struct program* prog) {
  const struct map_info* stats = NULL;
  for (size_t i = 0; i < prog->map_cnt; i++) {
    if (strcmp(prog->maps[i].name, "stats") == 0) {
      stats = &prog->maps[i];
    }
  }
  if (stats == NULL || stats->key_size != sizeof(uint32_t) ||
      stats->value_size != sizeof(struct stat_entry)) {
    return;
  }

  printf("%-16s %10s %10s\n", "IP", "passed", "dropped");
  uint32_t key = 0;
  uint32_t next = 0;
  int have_key = 0;
  for (;;) {
    union bpf_attr attr;
    memset(&attr, 0, sizeof attr);
    attr.map_fd = (uint32_t)stats->fd;
    attr.key = have_key ? (uint64_t)(uintptr_t)&key : 0;
    attr.next_key = (uint64_t)(uintptr_t)&next;
    if (sys_bpf(BPF_MAP_GET_NEXT_KEY, &attr, sizeof attr) < 0) {
      break; /* ключи закончились */
    }
    key = next;
    have_key = 1;

    struct stat_entry value;
    memset(&attr, 0, sizeof attr);
    attr.map_fd = (uint32_t)stats->fd;
    attr.key = (uint64_t)(uintptr_t)&key;
    attr.value = (uint64_t)(uintptr_t)&value;
    if (sys_bpf(BPF_MAP_LOOKUP_ELEM, &attr, sizeof attr) < 0) {
      continue;
    }
    struct in_addr ip = {.s_addr = key};
    printf(
        "%-16s %10llu %10llu\n",
        inet_ntoa(ip),
        (unsigned long long)value.passed,
        (unsigned long long)value.dropped
    );
  }
  fflush(stdout);
}

/* ------------------------------------------------------------------------- */
/* Режимы работы                                                             */
/* ------------------------------------------------------------------------- */

static int mode_test(
    struct program* prog, uint32_t src_be, int count, uint16_t proto
) {
  uint8_t pkt[64];
  size_t len = build_packet(pkt, src_be, proto);
  int counters[8] = {0};
  uint32_t last = 0;
  for (int i = 0; i < count; i++) {
    uint32_t ret = 0;
    if (test_run(prog->fd, pkt, len, &ret) < 0) {
      die_errno("BPF_PROG_TEST_RUN");
    }
    if (ret < 8) {
      counters[ret]++;
    }
    last = ret;
  }
  printf("result of last packet: %s (%u)\n", action_name(last), last);
  printf("pass=%d drop=%d\n", counters[XDP_PASS], counters[XDP_DROP]);
  print_stats(prog);
  return 0;
}

static void on_signal(int sig) {
  (void)sig;
  stop = 1;
}

static int mode_attach(
    struct program* prog, const char* ifname, int skb, int seconds
) {
  unsigned int ifindex = if_nametoindex(ifname);
  if (ifindex == 0) {
    die_errno(ifname);
  }

  union bpf_attr attr;
  memset(&attr, 0, sizeof attr);
  attr.link_create.prog_fd = (uint32_t)prog->fd;
  attr.link_create.target_ifindex = ifindex;
  attr.link_create.attach_type = BPF_XDP;
  attr.link_create.flags = skb ? XDP_FLAGS_SKB_MODE : 0;
  int link_fd = sys_bpf(BPF_LINK_CREATE, &attr, sizeof attr);
  if (link_fd < 0) {
    die_errno("BPF_LINK_CREATE");
  }
  printf(
      "attached to %s (ifindex %u), press Ctrl-C to detach\n", ifname, ifindex
  );
  fflush(stdout);

  signal(SIGINT, on_signal);
  signal(SIGTERM, on_signal);

  time_t deadline = seconds > 0 ? time(NULL) + seconds : 0;
  while (!stop && (deadline == 0 || time(NULL) < deadline)) {
    sleep(1);
    if (!stop) {
      print_stats(prog);
    }
  }
  /* Закрытие link_fd отсоединяет программу от интерфейса. */
  close(link_fd);
  printf("detached\n");
  return 0;
}

int main(int argc, char** argv) {
  const char* path = NULL;
  const char* ifname = NULL;
  int test = 0;
  int skb = 0;
  int seconds = 0;
  int count = 1;
  uint16_t proto = 0x0800;
  uint32_t src_be = inet_addr("10.200.1.2");

  for (int i = 1; i < argc; i++) {
    if (strcmp(argv[i], "--test") == 0) {
      test = 1;
    } else if (strcmp(argv[i], "-v") == 0) {
      verbose = 1;
    } else if (strcmp(argv[i], "--skb") == 0) {
      skb = 1;
    } else if (strcmp(argv[i], "--seconds") == 0 && i + 1 < argc) {
      seconds = atoi(argv[++i]);
    } else if (strcmp(argv[i], "--count") == 0 && i + 1 < argc) {
      count = atoi(argv[++i]);
    } else if (strcmp(argv[i], "--proto") == 0 && i + 1 < argc) {
      proto = (uint16_t)strtoul(argv[++i], NULL, 16);
    } else if (strcmp(argv[i], "--src") == 0 && i + 1 < argc) {
      src_be = inet_addr(argv[++i]);
    } else if (path == NULL) {
      path = argv[i];
    } else if (ifname == NULL) {
      ifname = argv[i];
    } else {
      path = NULL;
      break;
    }
  }
  if (path == NULL || (!test && ifname == NULL)) {
    fprintf(
        stderr,
        "usage: %s --test <prog> [--src IP] [--count N] [--proto HEX] [-v]\n"
        "       %s <prog> <iface> [--skb] [--seconds N] [-v]\n",
        argv[0],
        argv[0]
    );
    return 2;
  }

  struct program prog;
  program_from_file(&prog, path);
  load_program(&prog, "xdp_prog");
  free(prog.insns); /* ядро сделало свою копию байткода */
  prog.insns = NULL;
  printf(
      "program loaded: %zu instructions, %zu map(s)\n",
      prog.insn_cnt,
      prog.map_cnt
  );

  return test ? mode_test(&prog, src_be, count, proto)
              : mode_attach(&prog, ifname, skb, seconds);
}
