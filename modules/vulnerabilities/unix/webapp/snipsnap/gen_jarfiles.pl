#!/usr/bin/perl

$_ = `find . -name '*.jar' -print`;
print join(':', split("\n", $_));
