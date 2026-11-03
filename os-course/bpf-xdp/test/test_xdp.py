"""Тесты лабораторной BPF/XDP.

Нужны права root (загрузка BPF-программ). Запуск:
    sudo python3 -m unittest discover -s test -v

Тесты двух видов:
  * TestRun - BPF_PROG_TEST_RUN: программе подают заранее собранный пакет, сети
    не нужно;
  * End2End - настоящая сеть из двух veth в отдельном namespace, пакеты идут
    через ping.
"""
import os
import re
import shutil
import subprocess
import sys
import time
import unittest

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LOADER = os.path.join(ROOT, "load_xdp")
LIBBPF_LOADER = os.path.join(ROOT, "rps_libbpf")
IS_ROOT = os.geteuid() == 0


def obj(name):
    return os.path.join(ROOT, "bpf", name + ".bpf.o")


def sh(args, **kw):
    return subprocess.run(args, capture_output=True, text=True, **kw)


def build():
    r = sh(["make", "-C", ROOT])
    if r.returncode != 0:
        raise RuntimeError("make failed:\n" + r.stdout + r.stderr)


def setUpModule():
    build()


def test_run(prog, src="10.200.1.2", count=1, proto="0800"):
    r = sh([LOADER, "--test", obj(prog), "--src", src, "--count", str(count),
            "--proto", proto])
    return r


def last_action(r):
    return re.search(r"result of last packet: (\w+)", r.stdout).group(1)


def counts(r):
    m = re.search(r"pass=(\d+) drop=(\d+)", r.stdout)
    return int(m.group(1)), int(m.group(2))


@unittest.skipUnless(IS_ROOT, "нужен root")
class TestRun(unittest.TestCase):
    def test_pass_passes_everything(self):
        self.assertEqual(last_action(test_run("pass")), "XDP_PASS")

    def test_bad_is_rejected_by_verifier(self):
        r = sh([LOADER, "--test", obj("bad")])
        self.assertNotEqual(r.returncode, 0)
        self.assertIn("Permission denied", r.stderr)
        self.assertIn("invalid access to packet", r.stderr)

    def test_infinite_loop_is_rejected_by_verifier(self):
        r = sh([LOADER, "--test", obj("loop")])
        self.assertNotEqual(r.returncode, 0)
        self.assertIn("infinite loop detected", r.stderr)

    def test_drop_ipv4_drops_ipv4(self):
        self.assertEqual(last_action(test_run("drop_ipv4")), "XDP_DROP")

    def test_drop_ipv4_passes_arp(self):
        self.assertEqual(last_action(test_run("drop_ipv4", proto="0806")), "XDP_PASS")

    def test_block_ip_drops_blocked_source(self):
        self.assertEqual(last_action(test_run("block_ip", src="10.200.1.2")), "XDP_DROP")

    def test_block_ip_passes_other_source(self):
        self.assertEqual(last_action(test_run("block_ip", src="10.200.1.3")), "XDP_PASS")

    def test_block_ip_passes_neighbour_addresses(self):
        for ip in ("10.200.1.1", "10.200.1.20", "10.200.2.2", "11.200.1.2"):
            with self.subTest(ip=ip):
                self.assertEqual(last_action(test_run("block_ip", src=ip)), "XDP_PASS")

    def test_block_ip_passes_arp_from_blocked_address(self):
        r = test_run("block_ip", src="10.200.1.2", proto="0806")
        self.assertEqual(last_action(r), "XDP_PASS")

    def test_rps_allows_five_then_drops(self):
        r = test_run("rps", count=10)
        self.assertEqual(counts(r), (5, 5))

    def test_rps_below_limit_all_pass(self):
        self.assertEqual(counts(test_run("rps", count=5)), (5, 0))

    def test_rps_first_packet_passes(self):
        self.assertEqual(last_action(test_run("rps", count=1)), "XDP_PASS")

    def test_rps_statistics_per_ip(self):
        r = test_run("rps", src="10.200.1.2", count=8)
        self.assertRegex(r.stdout, r"10\.200\.1\.2\s+5\s+3")

    def test_rps_passes_arp(self):
        self.assertEqual(counts(test_run("rps", count=20, proto="0806")), (20, 0))

    def test_rps_each_loader_run_has_fresh_maps(self):
        # Каждый запуск загрузчика создаёт свои карты, и лимит действует заново.
        self.assertEqual(counts(test_run("rps", count=6)), (5, 1))
        self.assertEqual(counts(test_run("rps", count=6)), (5, 1))

    def test_raw_bin_program_loads(self):
        out = os.path.join("/tmp", "xdp_pass_test.bin")
        r = sh(["llvm-objcopy", "--dump-section", "xdp=" + out, obj("pass"),
                "/tmp/xdp_pass_test.o"])
        self.assertEqual(r.returncode, 0, r.stderr)
        r = sh([LOADER, "--test", out])
        self.assertEqual(last_action(r), "XDP_PASS")

    def test_loader_rejects_garbage_file(self):
        path = "/tmp/xdp_garbage.bin"
        with open(path, "wb") as f:
            f.write(b"123")
        r = sh([LOADER, "--test", path])
        self.assertNotEqual(r.returncode, 0)

    def test_loader_rejects_unknown_interface(self):
        r = sh([LOADER, obj("pass"), "no-such-if0"])
        self.assertNotEqual(r.returncode, 0)


def ping(src, count=3, interval=None, wait=1):
    cmd = ["ip", "netns", "exec", "xdp-ns", "ping", "-I", src, "-c", str(count),
           "-W", str(wait)]
    if interval:
        cmd += ["-i", str(interval)]
    cmd.append("10.200.1.1")
    r = sh(cmd)
    m = re.search(r"(\d+) packets transmitted, (\d+) received", r.stdout)
    return int(m.group(1)), int(m.group(2))


@unittest.skipUnless(IS_ROOT and shutil.which("ip"), "нужен root и iproute2")
class End2End(unittest.TestCase):
    def setUp(self):
        sh([os.path.join(ROOT, "scripts", "netns_down.sh")])
        r = sh([os.path.join(ROOT, "scripts", "netns_up.sh")])
        self.assertEqual(r.returncode, 0, r.stderr)
        self.proc = None

    def tearDown(self):
        if self.proc is not None and self.proc.poll() is None:
            self.proc.terminate()
            self.proc.communicate(timeout=10)
        sh([os.path.join(ROOT, "scripts", "netns_down.sh")])

    def attach(self, cmd):
        self.proc = subprocess.Popen(cmd, stdout=subprocess.PIPE,
                                     stderr=subprocess.PIPE, text=True)
        time.sleep(1.0)
        self.assertIsNone(self.proc.poll(), "загрузчик завершился слишком рано")

    def stop(self):
        self.proc.terminate()
        out, err = self.proc.communicate(timeout=10)
        return out

    def test_network_works_without_filter(self):
        self.assertEqual(ping("10.200.1.2"), (3, 3))
        self.assertEqual(ping("10.200.1.3"), (3, 3))

    def test_block_ip_blocks_only_blocked_address(self):
        self.attach([LOADER, obj("block_ip"), "xdp-host"])
        self.assertEqual(ping("10.200.1.2"), (3, 0))
        self.assertEqual(ping("10.200.1.3"), (3, 3))

    def test_filter_is_removed_after_loader_exits(self):
        self.attach([LOADER, obj("block_ip"), "xdp-host"])
        self.assertEqual(ping("10.200.1.2"), (3, 0))
        self.stop()
        time.sleep(0.5)
        self.assertEqual(ping("10.200.1.2"), (3, 3))

    def test_drop_ipv4_blocks_everything_but_arp_works(self):
        self.attach([LOADER, obj("drop_ipv4"), "xdp-host"])
        self.assertEqual(ping("10.200.1.2"), (3, 0))
        self.assertEqual(ping("10.200.1.3"), (3, 0))

    def _rps_check(self, cmd, other_ip_expected=True):
        self.attach(cmd)
        # 40 пакетов с интервалом 0.05 с (~2 секунды): допустимо около 5/с.
        sent, got = ping("10.200.1.2", count=40, interval=0.05, wait=1)
        self.assertEqual(sent, 40)
        self.assertGreaterEqual(got, 5)
        self.assertLessEqual(got, 20)
        # Другой адрес не пострадал от лимита соседа.
        self.assertEqual(ping("10.200.1.3"), (3, 3))
        time.sleep(1.2)
        out = self.stop()
        m = re.findall(r"10\.200\.1\.2\s+(\d+)\s+(\d+)", out)
        self.assertTrue(m, "в выводе нет статистики для 10.200.1.2:\n" + out)
        passed, dropped = map(int, m[-1])
        self.assertEqual(passed + dropped, 40)
        self.assertGreater(dropped, 0)

    def test_rps_window_resets_after_a_second(self):
        self.attach([LOADER, obj("rps"), "xdp-host"])
        # Пять пакетов укладываются в лимит; через секунду окно сбрасывается,
        # и ещё пять снова проходят.
        self.assertEqual(ping("10.200.1.2", count=5, interval=0.2), (5, 5))
        time.sleep(1.3)
        self.assertEqual(ping("10.200.1.2", count=5, interval=0.2), (5, 5))

    def test_rps_limiter_raw_loader(self):
        self._rps_check([LOADER, obj("rps"), "xdp-host"])

    @unittest.skipUnless(os.path.exists(LIBBPF_LOADER), "нет libbpf-версии")
    def test_rps_limiter_libbpf(self):
        self._rps_check([LIBBPF_LOADER, obj("rps_libbpf"), "xdp-host"])


if __name__ == "__main__":
    unittest.main()
