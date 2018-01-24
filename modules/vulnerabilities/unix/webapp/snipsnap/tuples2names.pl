#!/usr/bin/perl -w

$tuples = shift @ARGV;

while (scalar @ARGV) {
  $map = shift @ARGV;
  if ($map eq "none") {
    push @maps, undef;
    next;
  }
  open MAP, "<$map" or die;
  $map = { };
  $counter = 0;
  while (<MAP>) {
    chomp;
    $map->{$counter++} = $_;
  }
  $map->{"*"} = "*";
  close MAP;
  push @maps, $map;
}

open TUPLES, "<$tuples" or die;
while (<TUPLES>) {
  if (/^#/) {
    print;
    next;
  }
  @e = split;
  for ($i=0; $i<scalar @e; $i++) {
    if (defined $maps[$i]) {
      if (exists $maps[$i]{$e[$i]}) {
	print $maps[$i]{$e[$i]};
      }
      else {
	print "(no #$e[$i])";
      }
    }
    else {
      print $e[$i];
    }
    if ($i != (scalar @e-1)) {
      print " | ";
    }
  }
  print "\n";
}
close TUPLES;
