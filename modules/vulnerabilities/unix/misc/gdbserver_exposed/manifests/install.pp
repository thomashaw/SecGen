class gdbserver_exposed::install {
  # gdbserver ships in its own package on Debian, separate from gdb.
  ensure_packages(['gdbserver'])
}
