##########################################################################################
# SFX Glass Universal — MMT Extended Config
# Support: Magisk 24+ / KernelSU / APatch / KSU-Next
# Android 9 - 15, HIDL + AIDL auto-detect, arm64/arm neutral
##########################################################################################

# Android version limits
MINAPI=28
MAXAPI=36
MINMAGISK=24000

# Dynamic vendor lib
DYNLIB=true
DEBUG=false

# Replace list kosong — kita overlay, bukan replace
REPLACE=""

set_permissions() {
  set_perm_recursive $MODPATH/system/vendor/lib/soundfx 0 0 0755 0644
  chcon -R u:object_r:vendor_file:s0 $MODPATH/system/vendor/lib/soundfx 2>/dev/null
  if [ "$IS64BIT" ]; then
    set_perm_recursive $MODPATH/system/vendor/lib64/soundfx 0 0 0755 0644
    chcon -R u:object_r:vendor_file:s0 $MODPATH/system/vendor/lib64/soundfx 2>/dev/null
  fi
  # Legacy fallback
  if [ -d $MODPATH/system/lib/soundfx ]; then
    set_perm_recursive $MODPATH/system/lib/soundfx 0 0 0755 0644
  fi
  if [ -d $MODPATH/system/lib64/soundfx ]; then
    set_perm_recursive $MODPATH/system/lib64/soundfx 0 0 0755 0644
  fi
  # sepolicy
  set_perm $MODPATH/service.sh 0 0 0755
  set_perm $MODPATH/uninstall.sh 0 0 0755
}

# MMT Extended Logic
SKIPUNZIP=1
unzip -qjo "$ZIPFILE" 'common/functions.sh' -d $TMPDIR >&2
. $TMPDIR/functions.sh

# Detect root implementation — KSU/APatch need self-mount
if [ -n "$KSU" ] || [ -n "$KSU_NEXT" ] || [ -n "$APATCH" ] || [ -n "$APATCH_VER" ]; then
  ui_print "  → Detected KernelSU/APatch, enable self-mount"
  touch $MODPATH/skip_mount
  touch $MODPATH/skip_mountify
else
  ui_print "  → Magisk mode, overlay via Magic Mount"
  rm -f $MODPATH/skip_mount $MODPATH/skip_mountify
fi

# Banner
ui_print " "
ui_print "  ┌───────────────────────────────────────┐"
ui_print "  │  SFX Glass ViPER Universal v3.0       │"
ui_print "  │  iOS Liquid Glass • Dark/Light        │"
ui_print "  │  HIDL + AIDL Auto • Android 9-15      │"
ui_print "  └───────────────────────────────────────┘"
ui_print " "
