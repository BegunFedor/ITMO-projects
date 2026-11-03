# Сборка всех частей лабораторной работы.
#   make          - фильтры, загрузчик и (если есть libbpf) версия на libbpf
#   make clean    - убрать результаты сборки
ARCH_INC := /usr/include/$(shell gcc -print-multiarch)

BPF_CFLAGS := -O2 -target bpf -I$(ARCH_INC) -Ibpf
# -mcpu=v3 нужен rps для атомарного «прибавить и вернуть» (ядро 5.12+).
BPF_CPU_FLAGS := -mcpu=v3

SIMPLE := pass bad drop_ipv4 loop block_ip
OBJS := $(SIMPLE:%=bpf/%.bpf.o) bpf/rps.bpf.o

# Версия на libbpf собирается, только если libbpf-dev установлен.
HAVE_LIBBPF := $(shell pkg-config --exists libbpf 2>/dev/null && echo yes)
ifeq ($(HAVE_LIBBPF),yes)
OBJS += bpf/rps_libbpf.bpf.o
endif

all: load_xdp $(OBJS) $(if $(HAVE_LIBBPF),rps_libbpf)

# Фильтры. -g добавляет отладочную информацию и BTF (нужна libbpf-версии).
bpf/rps_libbpf.bpf.o: bpf/rps_libbpf.bpf.c bpf/common.h bpf/net.h
	clang -g $(BPF_CFLAGS) $(BPF_CPU_FLAGS) -c $< -o $@

bpf/rps.bpf.o: bpf/rps.bpf.c bpf/common.h bpf/net.h
	clang $(BPF_CFLAGS) $(BPF_CPU_FLAGS) -c $< -o $@

bpf/%.bpf.o: bpf/%.bpf.c bpf/net.h
	clang $(BPF_CFLAGS) -c $< -o $@

# «Голый» загрузчик: только glibc.
load_xdp: load_xdp.c bpf/common.h
	gcc -O2 -Wall -Wextra -o $@ $<

# Загрузчик на libbpf.
rps_libbpf: rps_libbpf.c bpf/common.h
	gcc -O2 -Wall -Wextra -o $@ $< $(shell pkg-config --cflags --libs libbpf)

test: all
	sudo python3 -m unittest discover -s test -v

clean:
	rm -f load_xdp rps_libbpf bpf/*.o

.PHONY: all test clean
