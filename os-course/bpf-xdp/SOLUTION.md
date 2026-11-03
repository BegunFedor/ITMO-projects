# Решение: BPF-24 (eBPF, XDP)

Задание: [README.md](./README.md).

## Что внутри

| Файл | Что это |
|---|---|
| `bpf/pass.bpf.c` | фильтр, пропускающий всё |
| `bpf/bad.bpf.c` | фильтр, который отвергает verifier (нет проверки границ) |
| `bpf/drop_ipv4.bpf.c` | исправленный `bad`: блокирует любые IPv4-кадры |
| `bpf/loop.bpf.c` | зацикленная программа (verifier отвергает) |
| `bpf/block_ip.bpf.c` | блокировка пакетов от `10.200.1.2` |
| `bpf/rps.bpf.c` | ограничитель RPS: 5 пакетов/с на IP + статистика (maps) |
| `bpf/rps_libbpf.bpf.c` | то же для libbpf |
| `load_xdp.c` | загрузчик на «голых» syscall `bpf()` (без libbpf и libelf) |
| `rps_libbpf.c` | загрузчик на libbpf |
| `scripts/` | поднять/убрать тестовую сеть, показательный прогон |
| `test/test_xdp.py` | 25 автотестов |

## Сборка и запуск

```sh
sudo apt install clang llvm gcc make pkg-config libbpf-dev iproute2 linux-tools-generic
make
sudo scripts/demo.sh                                   # показательный прогон
sudo python3 -m unittest discover -s test -v           # тесты
```

Пошагово:

```sh
sudo scripts/netns_up.sh
sudo ./load_xdp bpf/block_ip.bpf.o xdp-host &          # подключить фильтр
sudo ip netns exec xdp-ns ping -I 10.200.1.2 -c 3 10.200.1.1   # не проходит
sudo ip netns exec xdp-ns ping -I 10.200.1.3 -c 3 10.200.1.1   # проходит
sudo kill %1                                           # отсоединить фильтр
sudo scripts/netns_down.sh
```

`load_xdp` принимает объектный файл `.o` (нужен, если есть maps) или сырой байткод `.bin`
(`llvm-objcopy --dump-section xdp=prog.bin prog.bpf.o`). Режим `--test` прогоняет программу
через `BPF_PROG_TEST_RUN` без сети: `sudo ./load_xdp --test bpf/rps.bpf.o --count 10`.
