// Tests for "#!" (shebang) support in exec().
//
// Every test creates a small script, runs it with exec() in a child process
// whose stdout is a pipe, and compares what the child printed (and whether
// exec() succeeded) with what we expect.

#include "kernel/types.h"
#include "kernel/stat.h"
#include "kernel/fcntl.h"
#include "user/user.h"

enum { OUTSZ = 512 };

static int failed;

// Create file `name` with `len` bytes of `data`.
static void
mkfile(char *name, char *data, int len)
{
  int fd = open(name, O_CREATE | O_WRONLY | O_TRUNC);
  if (fd < 0) {
    printf("cannot create %s\n", name);
    exit(1);
  }
  if (write(fd, data, len) != len) {
    printf("cannot write %s\n", name);
    exit(1);
  }
  close(fd);
}

static void
mkscript(char *name, char *text)
{
  mkfile(name, text, strlen(text));
}

// exec(path, argv) in a child; collect what it prints into out.
// Returns the child's exit status: 77 means "exec() itself failed".
static int
capture(char *path, char **argv, char *out)
{
  int p[2], n, total = 0, status;

  if (pipe(p) < 0) {
    printf("pipe failed\n");
    exit(1);
  }
  int pid = fork();
  if (pid < 0) {
    printf("fork failed\n");
    exit(1);
  }
  if (pid == 0) {
    close(1);
    dup(p[1]);
    close(p[0]);
    close(p[1]);
    exec(path, argv);
    exit(77); // exec returned: it failed
  }
  close(p[1]);
  while ((n = read(p[0], out + total, OUTSZ - 1 - total)) > 0)
    total += n;
  out[total] = 0;
  close(p[0]);
  wait(&status);
  return status;
}

// Run path with argv and check the output and the exit status.
static void
check(char *name, char *path, char **argv, char *want, int want_status)
{
  static char out[OUTSZ];
  int status = capture(path, argv, out);

  if (status == want_status && strcmp(out, want) == 0) {
    printf("%s: OK\n", name);
  } else {
    printf("%s: FAILED (status %d, want %d; output \"%s\", want \"%s\")\n",
           name, status, want_status, out, want);
    failed = 1;
  }
}

static void
check_fails(char *name, char *path, char **argv)
{
  check(name, path, argv, "", 77);
}

int
main(void)
{
  printf("shebangtests: start\n");

  // 1. The simplest script: the interpreter is /echo, so it prints its
  //    arguments: the script path first, then the rest of our argv.
  mkscript("sb1", "#!/echo\n");
  check("basic", "sb1", (char *[]){"sb1", "a", "b", 0}, "sb1 a b\n", 0);

  // 2. One optional argument for the interpreter comes before the script.
  mkscript("sb2", "#!/echo X\n");
  check("optarg", "sb2", (char *[]){"sb2", "a", 0}, "X sb2 a\n", 0);

  // 3. Blanks after "#!", between words and at the end of the line.
  //    Everything after the interpreter is ONE argument (like Linux).
  mkscript("sb3", "#!  \t/echo \t Y  Z \t \n");
  check("spaces", "sb3", (char *[]){"sb3", 0}, "Y  Z sb3\n", 0);

  // 4. No newline at the end of the file.
  mkscript("sb4", "#!/echo");
  check("no-newline", "sb4", (char *[]){"sb4", 0}, "sb4\n", 0);

  // 5. The old argv[0] is replaced by the script path.
  mkscript("sb5", "#!/echo\n");
  check("argv0", "sb5", (char *[]){"fake", "a", 0}, "sb5 a\n", 0);

  // 6. The interpreter can itself be a script.
  mkscript("sb6a", "#!/echo\n");
  mkscript("sb6b", "#!sb6a\n");
  check("nested", "sb6b", (char *[]){"sb6b", "q", 0}, "sb6a sb6b q\n", 0);

  // 7. A script that is its own interpreter must fail, not loop forever.
  mkscript("sb7", "#!sb7\n");
  check_fails("self-loop", "sb7", (char *[]){"sb7", 0});

  // 8. Four nested scripts work, five do not.
  mkscript("c0", "#!/echo\n");
  mkscript("c1", "#!c0\n");
  mkscript("c2", "#!c1\n");
  mkscript("c3", "#!c2\n");
  mkscript("c4", "#!c3\n");
  check("depth-4", "c3", (char *[]){"c3", 0}, "c0 c1 c2 c3\n", 0);
  check_fails("depth-5", "c4", (char *[]){"c4", 0});

  // 9. The interpreter does not exist.
  mkscript("sb9", "#!/no/such/interpreter\n");
  check_fails("no-interpreter", "sb9", (char *[]){"sb9", 0});

  // 10. "#!" without an interpreter name.
  mkscript("sb10a", "#!\n");
  mkscript("sb10b", "#!   \n");
  check_fails("empty-a", "sb10a", (char *[]){"sb10a", 0});
  check_fails("empty-b", "sb10b", (char *[]){"sb10b", 0});

  // 11. The first line may be at most 128 bytes including the newline.
  {
    static char line[256];
    int i, n;
    strcpy(line, "#!/echo ");
    n = strlen(line);
    for (i = 0; i < 119; i++)
      line[n++] = 'a';
    line[n++] = '\n'; // 8 + 119 + 1 = 128 bytes: fits exactly
    mkfile("sb11a", line, n);
    static char want[OUTSZ];
    want[0] = 0;
    for (i = 0; i < 119; i++)
      want[i] = 'a';
    strcpy(want + 119, " sb11a\n");
    check("line-128", "sb11a", (char *[]){"sb11a", 0}, want, 0);

    line[n - 1] = 'a';
    line[n++] = '\n'; // 129 bytes: too long
    mkfile("sb11b", line, n);
    check_fails("line-129", "sb11b", (char *[]){"sb11b", 0});
  }

  // 12. Files that start with '#' but not with "#!", or plain text, are not
  //     scripts; they are not ELF either, so exec() fails as before.
  mkscript("sb12a", "#/echo\n");
  mkscript("sb12b", "hello\n");
  check_fails("hash-only", "sb12a", (char *[]){"sb12a", 0});
  check_fails("plain-text", "sb12b", (char *[]){"sb12b", 0});

  // 13. Ordinary programs still run.
  check("elf", "/echo", (char *[]){"echo", "hi", 0}, "hi\n", 0);

  // 14. Argument limit: kernel allows at most MAXARG-1 = 31 argv entries.
  //     The script path and the interpreter use two of them.
  mkscript("sb14", "#!/echo\n");
  {
    char *av[33];
    int i, k;
    for (k = 30; k <= 31; k++) { // k = number of entries in the old argv
      av[0] = "sb14";
      for (i = 1; i < k; i++)
        av[i] = "x";
      av[k] = 0;
      if (k == 30) {
        static char want[OUTSZ];
        strcpy(want, "sb14");
        for (i = 1; i < k; i++)
          strcpy(want + strlen(want), " x");
        strcpy(want + strlen(want), "\n");
        check("args-30", "sb14", av, want, 0);
      } else {
        check_fails("args-31", "sb14", av);
      }
    }
  }

  // 15. A script run by our shell: "#!/sh" and comment lines are skipped.
  mkscript("sb15", "#!/sh\n# a comment\n/echo from-sh\n");
  check("sh-script", "sb15", (char *[]){"sb15", 0}, "from-sh\n", 0);

  // 16. Many runs in a row (the scratch page must be freed every time).
  {
    int i, ok = 1;
    static char out[OUTSZ];
    for (i = 0; i < 300; i++) {
      int st = capture("sb1", (char *[]){"sb1", "z", 0}, out);
      if (st != 0 || strcmp(out, "sb1 z\n") != 0)
        ok = 0;
      st = capture("sb7", (char *[]){"sb7", 0}, out); // failing path
      if (st != 77)
        ok = 0;
    }
    if (ok) {
      printf("repeat: OK\n");
    } else {
      printf("repeat: FAILED\n");
      failed = 1;
    }
  }

  // Clean up.
  char *files[] = {"sb1",   "sb2",   "sb3",   "sb4",   "sb5",   "sb6a",
                   "sb6b",  "sb7",   "c0",    "c1",    "c2",    "c3",
                   "c4",    "sb9",   "sb10a", "sb10b", "sb11a", "sb11b",
                   "sb12a", "sb12b", "sb14",  "sb15"};
  for (int i = 0; i < sizeof(files) / sizeof(files[0]); i++)
    unlink(files[i]);

  if (failed) {
    printf("shebangtests: FAILED\n");
    exit(1);
  }
  printf("shebangtests: ALL OK\n");
  exit(0);
}
