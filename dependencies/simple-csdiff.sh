#!/bin/bash

parentFolder=$(echo "${1%/*}")

sed 's/{/\n$$$$$$${\n$$$$$$$/g' $1 | sed 's/}/\n$$$$$$$}\n$$$$$$$/g' | sed 's/(/\n$$$$$$$(\n$$$$$$$/g' | sed 's/)/\n$$$$$$$)\n$$$$$$$/g' | sed 's/;/\n$$$$$$$;\n$$$$$$$/g' | sed 's/,/\n$$$$$$$,\n$$$$$$$/g' > "$1_temp"
sed 's/{/\n$$$$$$${\n$$$$$$$/g' $2 | sed 's/}/\n$$$$$$$}\n$$$$$$$/g' | sed 's/(/\n$$$$$$$(\n$$$$$$$/g' | sed 's/)/\n$$$$$$$)\n$$$$$$$/g' | sed 's/;/\n$$$$$$$;\n$$$$$$$/g' | sed 's/,/\n$$$$$$$,\n$$$$$$$/g' > "$2_temp"
sed 's/{/\n$$$$$$${\n$$$$$$$/g' $3 | sed 's/}/\n$$$$$$$}\n$$$$$$$/g' | sed 's/(/\n$$$$$$$(\n$$$$$$$/g' | sed 's/)/\n$$$$$$$)\n$$$$$$$/g' | sed 's/;/\n$$$$$$$;\n$$$$$$$/g' | sed 's/,/\n$$$$$$$,\n$$$$$$$/g' > "$3_temp"

diff3 -E -L MINE -L BASE -L YOURS -m "$1_temp" "$2_temp" "$3_temp" > "$parentFolder/mid_merged"

rm "$1_temp"
rm "$2_temp"
rm "$3_temp"

sed ':a;N;$!ba;s/\n\$\$\$\$\$\$\$//g' "$parentFolder/mid_merged" > "$parentFolder/merged"

rm "$parentFolder/mid_merged"

ESCAPED_LEFT=$(printf '%s\n' "$1" | sed -e 's/[\/&]/\\&/g')
ESCAPED_BASE=$(printf '%s\n' "$2" | sed -e 's/[\/&]/\\&/g')
ESCAPED_RIGHT=$(printf '%s\n' "$3" | sed -e 's/[\/&]/\\&/g')

ESCAPED_TEMP_LEFT=$(printf '%s\n' "$1_temp" | sed -e 's/[\/&]/\\&/g')
ESCAPED_TEMP_BASE=$(printf '%s\n' "$2_temp" | sed -e 's/[\/&]/\\&/g')
ESCAPED_TEMP_RIGHT=$(printf '%s\n' "$3_temp" | sed -e 's/[\/&]/\\&/g')

sed "s/\(<<<<<<< MINE\)\(.\+\)/\1\n\2/" "$parentFolder/merged" \
| sed "s/\(<<<<<<< BASE\)\(.\+\)/\1\n\2/" \
| sed "s/\(<<<<<<< YOURS\)\(.\+\)/\1\n\2/" \
| sed "s/\(||||||| BASE\)\(.\+\)/\1\n\2/" \
| sed "s/\(||||||| MINE\)\(.\+\)/\1\n\2/" \
| sed "s/\(||||||| YOURS\)\(.\+\)/\1\n\2/" \
| sed "s/\(>>>>>>> YOURS\)\(.\+\)/\1\n\2/" \
| sed "s/\(>>>>>>> MINE\)\(.\+\)/\1\n\2/" \
| sed "s/\(>>>>>>> BASE\)\(.\+\)/\1\n\2/" \
| sed "s/\(=======\)\(.\+\)/\1\n\2/" > $4

rm "$parentFolder/merged"

