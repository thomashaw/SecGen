#!/bin/sh
# Download the Ubuntu 22.04 (jammy) Snort 2.9.15.1 packages and verify their SHA256s
# (from the jammy universe Packages index). Usage: download_snort_debs.sh <dir>
set -e
cd "$1"
base=http://archive.ubuntu.com/ubuntu/pool/universe/s/snort
while read -r name sum; do
  [ -z "$name" ] && continue
  wget -q --tries=5 --timeout=60 -O "$name" "$base/$name"
  echo "$sum  $name" | sha256sum -c -
done <<LIST
snort_2.9.15.1-6build1_amd64.deb d603bb58890fc47d112bcbdf8372f5297098dc80bdde7f5f02e386b93bf09b29
snort-common_2.9.15.1-6build1_all.deb 46f683fcec5a3827404318c5a5b48c6d72b2e4cad04e640f9f646c828fad7944
snort-common-libraries_2.9.15.1-6build1_amd64.deb 0422ae18ded0b4524804769162428d9179ff011ebe19d2187a2539ae7ae08137
snort-rules-default_2.9.15.1-6build1_all.deb 3db200cd0f9eef57f71936b66369bf756a0a12c4e2a32b369e2f7a571ae31a18
LIST
touch .verified
