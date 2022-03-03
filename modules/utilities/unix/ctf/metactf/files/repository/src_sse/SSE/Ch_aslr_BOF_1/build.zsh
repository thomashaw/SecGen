#!/bin/zsh
SALT=`date +%N`
if [[ ARGC -gt 0 ]] then
  BINNAME=`basename $PWD`
  foreach USER ($@)
    mkdir -p obj/$USER
    AA=`echo $USER $SALT $BINNAME | sha512sum | sum | cut -c 1-2 | awk '{printf "%d",$1+10}'`
    BB=`echo $USER $SALT $BINNAME | openssl dgst -sha512 -binary | base64 | head -1 | tr -d /=+ | cut -c 1-3 | xxd -p | sed s/0a$/7a/`

    cat program.c.template | sed s/AAAAAA/$AA/ >! program.c
    gcc -g -m32 -fno-stack-protector -z norelro -Wl,--section-start=.text=0x$BB -mpreferred-stack-boundary=2 -o obj/$USER/$BINNAME program.c
  end
else
  echo "USAGE: build.zsh <user_email(s)>"
fi
