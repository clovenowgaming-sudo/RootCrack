#!/system/bin/sh
# Invoked by the app to stage a module zip through the rfd daemon.
# Usage: module_installer.sh <daemon_path> <superkey> <zip>
DAEMON="$1"
KEY="$2"
ZIP="$3"
"$DAEMON" --key "$KEY" module install "$ZIP"
