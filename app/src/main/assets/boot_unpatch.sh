#!/system/bin/sh
#######################################################################################
# RootCrack Boot Image Unpatcher — restores a RootCrack-patched image or
# flashes back the saved ori.img backup.
#######################################################################################

. ./util_functions.sh

BOOTIMAGE="$1"
require_file "$BOOTIMAGE"
command -v ./kptools >/dev/null 2>&1 || abort "kptools not found"

if [ -f ori.img ]; then
    ui_print "- Restoring from ori.img backup"
    cp ori.img "$BOOTIMAGE" || abort "restore failed"
    ui_print "- Restored."
    exit 0
fi

ui_print "- No ori.img backup found, attempting kptools unpatch"
./kptools unpack "$BOOTIMAGE" || abort "unpack failed ($?)"
./kptools -p -i kernel -r -o kernel.restored || abort "unpatch failed ($?)"
mv kernel.restored kernel
./kptools repack "$BOOTIMAGE" || abort "repack failed ($?)"
ui_print "- Unpatched image ready: new-boot.img"
