/*
 * rps_libbpf - загрузчик RPS-ограничителя на libbpf.
 *
 * Сравните с load_xdp.c: то, что там занимает сотни строк (разбор ELF, создание
 * карт, релокации, BPF_PROG_LOAD, BPF_LINK_CREATE), здесь делают несколько
 * вызовов библиотеки.
 *
 * Использование: rps_libbpf <rps_libbpf.bpf.o> <iface> [--seconds N]
 */
#define _GNU_SOURCE

#include <arpa/inet.h>
#include <bpf/bpf.h>
#include <bpf/libbpf.h>
#include <errno.h>
#include <net/if.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>

#include "bpf/common.h"

static volatile sig_atomic_t stop = 0;

static void on_signal(int sig) {
  (void)sig;
  stop = 1;
}

static void print_stats(int stats_fd) {
  printf("%-16s %10s %10s\n", "IP", "passed", "dropped");
  __u32 key = 0;
  __u32 next = 0;
  int have_key = 0;
  /* Обходим все ключи карты: «дай следующий ключ после текущего». */
  while (bpf_map_get_next_key(stats_fd, have_key ? &key : NULL, &next) == 0) {
    key = next;
    have_key = 1;
    struct stat_entry value;
    if (bpf_map_lookup_elem(stats_fd, &key, &value) != 0) {
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

int main(int argc, char** argv) {
  if (argc < 3) {
    fprintf(
        stderr, "usage: %s <rps_libbpf.bpf.o> <iface> [--seconds N]\n", argv[0]
    );
    return 2;
  }
  const char* path = argv[1];
  const char* ifname = argv[2];
  int seconds = 0;
  if (argc >= 5 && strcmp(argv[3], "--seconds") == 0) {
    seconds = atoi(argv[4]);
  }

  unsigned int ifindex = if_nametoindex(ifname);
  if (ifindex == 0) {
    perror(ifname);
    return 1;
  }

  /* 1. Открыть объектный файл: libbpf разберёт ELF, секции и карты. */
  struct bpf_object* obj = bpf_object__open_file(path, NULL);
  if (obj == NULL) {
    fprintf(stderr, "cannot open %s: %s\n", path, strerror(errno));
    return 1;
  }

  /* 2. Загрузить: создать карты, подставить релокации, BPF_PROG_LOAD. */
  if (bpf_object__load(obj) != 0) {
    fprintf(stderr, "cannot load the object: %s\n", strerror(errno));
    return 1;
  }

  struct bpf_program* prog = bpf_object__find_program_by_name(obj, "rps");
  int stats_fd = bpf_object__find_map_fd_by_name(obj, "stats");
  if (prog == NULL || stats_fd < 0) {
    fprintf(stderr, "program 'rps' or map 'stats' not found\n");
    return 1;
  }

  /* 3. Подключить к интерфейсу (внутри - BPF_LINK_CREATE). */
  struct bpf_link* link = bpf_program__attach_xdp(prog, (int)ifindex);
  if (link == NULL) {
    fprintf(stderr, "cannot attach to %s: %s\n", ifname, strerror(errno));
    return 1;
  }
  printf("attached to %s (libbpf), press Ctrl-C to detach\n", ifname);
  fflush(stdout);

  signal(SIGINT, on_signal);
  signal(SIGTERM, on_signal);

  time_t deadline = seconds > 0 ? time(NULL) + seconds : 0;
  while (!stop && (deadline == 0 || time(NULL) < deadline)) {
    sleep(1);
    if (!stop) {
      print_stats(stats_fd);
    }
  }

  bpf_link__destroy(link); /* отсоединяет программу от интерфейса */
  bpf_object__close(obj);
  printf("detached\n");
  return 0;
}
