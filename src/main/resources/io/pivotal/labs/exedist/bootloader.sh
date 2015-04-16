#! /bin/sh -eu

UNPACK_DIR="$(mktemp -d "${TMPDIR:-/tmp}"/foobar.XXXXXXXX)"
unzip "$0" -d "$UNPACK_DIR" >"$UNPACK_DIR"/unpack.log 2>&1 || [ $? -eq 1 ]
exec "$UNPACK_DIR"/myapp/bin/myapp "$@"

