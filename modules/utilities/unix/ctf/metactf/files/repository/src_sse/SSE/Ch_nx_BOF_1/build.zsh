#!/bin/zsh
SALT=`date +%N`
if [[ ARGC -gt 0 ]] then
  BINNAME=`basename $PWD`
  foreach USER ($@)
    mkdir -p obj/$USER

    # No randomisation, this is a static example to demonstrate a stack-based exploit failing due to nx stack
    cat program.c.template > program.c
    gcc -g -m32 -fno-stack-protector -z norelro -fno-pie -no-pie -Wl,--section-start=.text=0x64574b7a -mpreferred-stack-boundary=2 -o obj/$USER/$BINNAME program.c
  end
else
  echo "USAGE: build.zsh <user_email(s)>"
fi
