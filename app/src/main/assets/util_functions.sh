#!/system/bin/sh
# Shared shell helpers for RootCrack's patch scripts.

ui_print() { echo "$1"; }

abort() {
    ui_print "! $1"
    exit 1
}

require_file() {
    [ -f "$1" ] || abort "$1 not found"
}
