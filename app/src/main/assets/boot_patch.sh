#!/system/bin/sh
#######################################################################################
# RootCrack Boot Image Patcher
#
# Usage: boot_patch.sh <superkey> <bootimage>
#
# Orchestration only: unpack -> verify CONFIG_KALLSYMS -> inject kpimg via
# kptools -> repack. All of the actual kernel-image-format and patch logic
# lives in ./kptools / ./kpimg, built upstream by the KernelPatch project —
# this script just sequences the calls and checks exit codes.
#######################################################################################

. ./util_functions.sh

SUPERKEY="$1"
BOOTIMAGE="$2"

[ -z "$SUPERKEY" ] && abort "SuperKey empty"
require_file "$BOOTIMAGE"
command -v ./kptools >/dev/null 2>&1 || abort "kptools not found"
require_file "./kpimg"

ui_print "**************************************"
ui_print " RootCrack Boot Image Patcher"
ui_print "**************************************"

ui_print "- Unpacking boot image"
./kptools unpack "$BOOTIMAGE" || abort "unpack failed ($?)"

./kptools -i kernel -f | grep -q CONFIG_KALLSYMS=y || {
    ui_print "- Aborted: kernel lacks CONFIG_KALLSYMS"
    exit 1
}

if ./kptools -i kernel -l | grep -q patched=false; then
    ui_print "- Backing up original boot image to ori.img"
    cp "$BOOTIMAGE" ori.img 2>/dev/null
fi

mv kernel kernel.ori

ui_print "- Patching kernel with kpimg"
KPT_ARGS=""
[ "$SUPERKEY" != "su" ] && KPT_ARGS="-S $SUPERKEY"
./kptools -p -i kernel.ori $KPT_ARGS -k kpimg -o kernel || abort "patch failed ($?)"

ui_print "- Repacking boot image"
./kptools repack "$BOOTIMAGE" || abort "repack failed ($?)"

if ! ./kptools -i kernel.ori -f | grep -q CONFIG_KALLSYMS_ALL=y; then
    ui_print "- Warning: CONFIG_KALLSYMS_ALL not set; device may fail to boot."
    ui_print "- Keep your ori.img backup handy."
fi

ui_print "- Patched image ready: new-boot.img"
