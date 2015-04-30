#!/bin/sh
# Uncompress a clash csv rule file
# see https://github.com/clanner/cocdp/wiki/Csv-Files
for f in "$@"
do
    if [ -f ${f} ] ; then
        (
            dd if="${f}" bs=1 count=9
            dd if=/dev/zero bs=1 count=4
            dd if="${f}" bs=1 skip=9
        ) | unlzma -dc > "${f}.txt"
    fi
done
